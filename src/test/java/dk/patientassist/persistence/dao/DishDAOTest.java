package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DishDAO} using JUnit 5.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DishDAOTest {

    private DishDAO dishDAO;

    @BeforeAll
    void setup() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    private DishDTO createTestDish(String name) {
        return new DishDTO(
                name,
                "Beskrivelse af retten: " + name,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                DishStatus.TILGÆNGELIG,
                500.0,
                20.0,
                40.0,
                15.0,
                Allergens.GLUTEN
        );
    }

    @Test
    @Order(1)
    void testCreateAndGetDish() {
        DishDTO dto = dishDAO.create(createTestDish("Mørbradgryde"));
        assertNotNull(dto.getId());

        Optional<DishDTO> fetched = dishDAO.get(dto.getId());
        assertTrue(fetched.isPresent());
        assertEquals("Mørbradgryde", fetched.get().getName());
    }

    @Test
    @Order(2)
    void testUpdateDish() {
        DishDTO dto = dishDAO.create(createTestDish("Gammeldags gryderet"));
        dto.setDescription("Opdateret beskrivelse");
        DishDTO updated = dishDAO.update(dto.getId(), dto);
        assertEquals("Opdateret beskrivelse", updated.getDescription());
    }

    @Test
    @Order(3)
    void testDeleteDish() {
        DishDTO dto = dishDAO.create(createTestDish("Citronfromage"));
        boolean deleted = dishDAO.delete(dto.getId());
        assertTrue(deleted);
        assertTrue(dishDAO.get(dto.getId()).isEmpty());
    }

    @Test
    @Order(4)
    void testUpdateDishField() {
        DishDTO dto = dishDAO.create(createTestDish("Pandekager"));
        dishDAO.updateDishField(dto.getId(), "kcal", 850.0);
        Optional<DishDTO> updated = dishDAO.get(dto.getId());
        assertTrue(updated.isPresent());
        assertEquals(850.0, updated.get().getKcal());
    }

    @Test
    @Order(5)
    void testGetDishesByStatus() {
        dishDAO.create(createTestDish("Torsk med remoulade"));
        List<DishDTO> list = dishDAO.getDishesByStatus(DishStatus.TILGÆNGELIG);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(d -> d.getStatus() == DishStatus.TILGÆNGELIG));
    }

    @Test
    @Order(6)
    void testGetDishesByAllergen() {
        dishDAO.create(createTestDish("Rugbrød med ost"));
        List<DishDTO> list = dishDAO.getDishesByAllergen(Allergens.GLUTEN);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(d -> d.getAllergens() == Allergens.GLUTEN));
    }

    @Test
    @Order(7)
    void testGetDishesByStatusAndAllergen() {
        dishDAO.create(createTestDish("Kyllingesalat"));
        List<DishDTO> list = dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(8)
    void testUpdateNonexistentDish() {
        DishDTO dto = createTestDish("Fiktiv ret");
        DishDTO result = dishDAO.update(999999, dto);
        assertNull(result);
    }

    @Test
    @Order(9)
    void testDeleteNonexistentDish() {
        assertFalse(dishDAO.delete(999999));
    }
}
