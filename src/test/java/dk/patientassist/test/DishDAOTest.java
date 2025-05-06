package dk.patientassist.test;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DishDAOTest {

    private static EntityManagerFactory emf;
    private static DishDAO dishDAO;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    static void setUp() {
        System.setProperty("DB_CONN_STR", postgres.getJdbcUrl().replace("jdbc:postgresql:", "jdbc:tc:postgresql:"));
        System.setProperty("DB_NAME", "");
        System.setProperty("DB_USER", "postgres");
        System.setProperty("DB_PW", "postgres");

        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();
        dishDAO = DishDAO.getInstance(emf);
    }

    @AfterEach
    void cleanUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Dish").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    @Order(1)
    void testCreateAndGetDish() {
        DishDTO dto = new DishDTO(
                "Rugbrødsmad", "Med leverpostej",
                LocalDate.now(), LocalDate.now().plusDays(5),
                DishStatus.TILGÆNGELIG, 200, 8, 30, 5, Allergens.GLUTEN
        );

        DishDTO created = dishDAO.create(dto);
        assertNotNull(created);
        assertNotNull(created.getId());

        DishDTO fetched = dishDAO.get(created.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals("Rugbrødsmad", fetched.getName());
        assertEquals(DishStatus.TILGÆNGELIG, fetched.getStatus());
    }

    @Test
    @Order(2)
    void testGetAllDishes() {
        dishDAO.create(new DishDTO(
                "A", "Desc A", LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 100, 10, 20, 5, Allergens.ÆG
        ));
        dishDAO.create(new DishDTO(
                "B", "Desc B", LocalDate.now(), LocalDate.now().plusDays(2),
                DishStatus.TILGÆNGELIG, 200, 15, 25, 10, Allergens.LAKTOSE
        ));

        List<DishDTO> all = dishDAO.getAll();
        assertEquals(2, all.size());
    }

    @Test
    @Order(3)
    void testDeleteDish() {
        DishDTO dto = new DishDTO(
                "DeleteTest", "To be deleted",
                LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 150, 5, 15, 3, Allergens.GLUTEN
        );
        DishDTO created = dishDAO.create(dto);
        boolean deleted = dishDAO.delete(created.getId());

        assertTrue(deleted);
        assertTrue(dishDAO.get(created.getId()).isEmpty());
    }

    @Test
    @Order(4)
    void testUpdateDishField() {
        DishDTO dto = new DishDTO(
                "Gammel ret", "Skal opdateres",
                LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 300, 10, 20, 5, Allergens.GLUTEN
        );
        DishDTO created = dishDAO.create(dto);

        dishDAO.updateDishField(created.getId(), "name", "Opdateret ret");
        dishDAO.updateDishField(created.getId(), "kcal", 450.0);

        DishDTO updated = dishDAO.get(created.getId()).orElseThrow();
        assertEquals("Opdateret ret", updated.getName());
        assertEquals(450.0, updated.getKcal());
    }

    @Test
    @Order(5)
    void testGetDishesByStatus() {
        dishDAO.create(new DishDTO(
                "Tilgængelig ret", "Desc", LocalDate.now(), LocalDate.now().plusDays(3),
                DishStatus.TILGÆNGELIG, 200, 8, 30, 5, Allergens.ÆG
        ));
        dishDAO.create(new DishDTO(
                "Udsolgt ret", "Desc", LocalDate.now(), LocalDate.now().plusDays(3),
                DishStatus.UDSOLGT, 300, 12, 25, 7, Allergens.LAKTOSE
        ));

        var available = dishDAO.getDishesByStatus(DishStatus.TILGÆNGELIG);
        var soldOut = dishDAO.getDishesByStatus(DishStatus.UDSOLGT);

        assertEquals(1, available.size());
        assertEquals("Tilgængelig ret", available.get(0).getName());

        assertEquals(1, soldOut.size());
        assertEquals("Udsolgt ret", soldOut.get(0).getName());
    }

    @Test
    @Order(6)
    void testGetDishesByAllergen() {
        dishDAO.create(new DishDTO(
                "Æggeret", "Med æg", LocalDate.now(), LocalDate.now().plusDays(3),
                DishStatus.TILGÆNGELIG, 250, 12, 20, 10, Allergens.ÆG
        ));
        dishDAO.create(new DishDTO(
                "Laktosefri ret", "Uden mælk", LocalDate.now(), LocalDate.now().plusDays(3),
                DishStatus.TILGÆNGELIG, 220, 10, 22, 8, Allergens.LAKTOSE
        ));

        var result = dishDAO.getDishesByAllergen(Allergens.ÆG);
        assertEquals(1, result.size());
        assertEquals("Æggeret", result.get(0).getName());
    }

    @Test
    @Order(7)
    void testGetDishesByStatusAndAllergen() {
        dishDAO.create(new DishDTO(
                "Tilgængelig m. æg", "En ret", LocalDate.now(), LocalDate.now().plusDays(2),
                DishStatus.TILGÆNGELIG, 210, 9, 23, 6, Allergens.ÆG
        ));
        dishDAO.create(new DishDTO(
                "Udsolgt m. æg", "En anden ret", LocalDate.now(), LocalDate.now().plusDays(2),
                DishStatus.UDSOLGT, 230, 11, 21, 7, Allergens.ÆG
        ));

        var filtered = dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.ÆG);
        assertEquals(1, filtered.size());
        assertEquals("Tilgængelig m. æg", filtered.get(0).getName());
    }

    @Test
    @Order(8)
    void testUpdateDishFieldWithInvalidField() {
        DishDTO created = dishDAO.create(new DishDTO(
                "Invalid patch", "Tester felt", LocalDate.now(), LocalDate.now().plusDays(3),
                DishStatus.TILGÆNGELIG, 310, 16, 26, 13, Allergens.LAKTOSE
        ));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dishDAO.updateDishField(created.getId(), "invalidFieldName", "value");
        });

        assertTrue(exception.getMessage().contains("Unsupported field"));
    }

    @Test
    @Order(9)
    void testUpdateNonExistentDish() {
        DishDTO input = new DishDTO("X", "desc", LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 100, 10, 20, 5, Allergens.SESAM);
        DishDTO result = dishDAO.update(9999, input); // assuming this ID doesn't exist
        assertNull(result);
    }

    @Test
    @Order(10)
    void testGetInvalidDishIdReturnsEmptyOptional() {
        var result = dishDAO.get(-1);
        assertTrue(result.isEmpty());
    }

}