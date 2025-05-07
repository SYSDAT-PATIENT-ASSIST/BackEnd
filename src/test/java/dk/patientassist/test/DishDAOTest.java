package dk.patientassist.test;

import dk.patientassist.config.Mode;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.test.utilities.TestUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DishDAOTest {

    private static EntityManagerFactory emf;
    private static DishDAO dishDAO;
    private static TestUtils testUtils;

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

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();
        dishDAO = DishDAO.getInstance(emf);
        testUtils = new TestUtils();
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
        DishDTO dto = testUtils.buildDishDTO("Rugbrødsmad", 200);

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
        dishDAO.create(testUtils.buildDishDTO("A", 100));
        dishDAO.create(testUtils.buildDishDTO("B", 200));

        List<DishDTO> all = dishDAO.getAll();
        assertEquals(2, all.size());
    }

    @Test
    @Order(3)
    void testDeleteDish() {
        DishDTO dto = testUtils.buildDishDTO("DeleteTest", 150);
        DishDTO created = dishDAO.create(dto);
        boolean deleted = dishDAO.delete(created.getId());

        assertTrue(deleted);
        assertTrue(dishDAO.get(created.getId()).isEmpty());
    }

    @Test
    @Order(4)
    void testUpdateDishField() {
        DishDTO dto = testUtils.buildDishDTO("Gammel ret", 300);
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
        dishDAO.create(testUtils.buildDishDTO("Tilgængelig ret", DishStatus.TILGÆNGELIG, Set.of(Allergens.ÆG)));
        dishDAO.create(testUtils.buildDishDTO("Udsolgt ret", DishStatus.UDSOLGT, Set.of(Allergens.LAKTOSE)));

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
        dishDAO.create(testUtils.buildDishDTO("Æggeret", DishStatus.TILGÆNGELIG, Set.of(Allergens.ÆG)));
        dishDAO.create(testUtils.buildDishDTO("Laktosefri ret", DishStatus.TILGÆNGELIG, Set.of(Allergens.LAKTOSE)));

        var result = dishDAO.getDishesByAllergen(Allergens.ÆG);
        assertEquals(1, result.size());
        assertEquals("Æggeret", result.get(0).getName());
    }

    @Test
    @Order(7)
    void testGetDishesByStatusAndAllergen() {
        dishDAO.create(testUtils.buildDishDTO("Tilgængelig m. æg", DishStatus.TILGÆNGELIG, Set.of(Allergens.ÆG)));
        dishDAO.create(testUtils.buildDishDTO("Udsolgt m. æg", DishStatus.UDSOLGT, Set.of(Allergens.ÆG)));

        var filtered = dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.ÆG);
        assertEquals(1, filtered.size());
        assertEquals("Tilgængelig m. æg", filtered.get(0).getName());
    }

    @Test
    @Order(8)
    void testUpdateDishFieldWithInvalidField() {
        DishDTO created = dishDAO.create(testUtils.buildDishDTO("Invalid patch", 310));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dishDAO.updateDishField(created.getId(), "invalidFieldName", "value");
        });

        assertTrue(exception.getMessage().contains("Unsupported field"));
    }

    @Test
    @Order(9)
    void testUpdateNonExistentDish() {
        DishDTO input = testUtils.buildDishDTO("X", 100);
        DishDTO result = dishDAO.update(9999, input); // assuming this ID doesn't exist
        assertNull(result);
    }

    @Test
    @Order(10)
    void testGetInvalidDishIdReturnsEmptyOptional() {
        Optional<DishDTO> result = dishDAO.get(-1);
        assertTrue(result.isEmpty());
    }
}
