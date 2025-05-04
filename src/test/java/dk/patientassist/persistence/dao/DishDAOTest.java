package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DishDAO} using JUnit 5.
 * Covers creation, retrieval, update, delete, and filtering operations for Dish entities.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DishDAOTest {

    private static DishDAO dishDAO;

    @BeforeAll
    static void setup() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    @BeforeEach
    void cleanUp() {
        dishDAO.getAllAvailableDishes().forEach(d -> dishDAO.deleteDish(d.getId()));
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
    void testCreateDish() {
        DishDTO created = dishDAO.createDish(createTestDish("Mørbradgryde"));
        assertNotNull(created.getId());
        assertEquals("Mørbradgryde", created.getName());
        assertEquals("Beskrivelse af retten: Mørbradgryde", created.getDescription());
    }

    @Test
    void testGetDish() {
        DishDTO dto = dishDAO.createDish(createTestDish("Brændende kærlighed"));
        DishDTO fetched = dishDAO.getDish(dto.getId());
        assertNotNull(fetched);
        assertEquals(dto.getId(), fetched.getId());
        assertEquals("Brændende kærlighed", fetched.getName());
    }

    @Test
    void testUpdateDish() {
        DishDTO original = dishDAO.createDish(createTestDish("Rødgrød med fløde"));
        original.setDescription("Opdateret: Klassisk dansk dessert");
        DishDTO updated = dishDAO.updateDish(original.getId(), original);
        assertEquals("Opdateret: Klassisk dansk dessert", updated.getDescription());
    }

    @Test
    void testDeleteDish() {
        DishDTO dto = dishDAO.createDish(createTestDish("Citronfromage"));
        DishDTO deleted = dishDAO.deleteDish(dto.getId());
        assertNotNull(deleted);
        assertNull(dishDAO.getDish(dto.getId()));
    }

    @Test
    void testFindDishByName() {
        dishDAO.createDish(createTestDish("Æbleflæsk"));
        DishDTO found = dishDAO.findDishByName("Æbleflæsk");
        assertNotNull(found);
        assertEquals("Æbleflæsk", found.getName());
    }

    @Test
    void testGetAllAvailableDishes() {
        dishDAO.createDish(createTestDish("Hakkebøf med bløde løg"));
        dishDAO.createDish(createTestDish("Stegt rødspætte"));
        List<DishDTO> dishes = dishDAO.getAllAvailableDishes();
        assertTrue(dishes.size() >= 2);
    }

    @Test
    void testUpdateDishStatus() {
        DishDTO dto = dishDAO.createDish(createTestDish("Fiskefrikadeller"));
        DishDTO updated = dishDAO.updateDishStatus(dto.getId(), DishStatus.UDSOLGT);
        assertEquals(DishStatus.UDSOLGT, updated.getStatus());
    }

    @Test
    void testUpdateDishAllergens() {
        DishDTO dto = dishDAO.createDish(createTestDish("Pandekager"));
        DishDTO updated = dishDAO.updateDishAllergens(dto.getId(), Allergens.LAKTOSE);
        assertEquals(Allergens.LAKTOSE, updated.getAllergens());
    }

    @Test
    void testUpdateDishName() {
        DishDTO dto = dishDAO.createDish(createTestDish("Forloren hare"));
        DishDTO updated = dishDAO.updateDishName(dto.getId(), "Ovnbagt laks med spinat");
        assertEquals("Ovnbagt laks med spinat", updated.getName());
    }

    @Test
    void testUpdateDishDescription() {
        DishDTO dto = dishDAO.createDish(createTestDish("Klassisk gryderet"));
        DishDTO updated = dishDAO.updateDishDescription(dto.getId(), "Langtidsstegt med rødvin og rodfrugter");
        assertEquals("Langtidsstegt med rødvin og rodfrugter", updated.getDescription());
    }

    @Test
    void testUpdateDishKcal() {
        DishDTO dto = dishDAO.createDish(createTestDish("Kalorieeksempel"));
        DishDTO updated = dishDAO.updateDishKcal(dto.getId(), 845.0);
        assertEquals(845.0, updated.getKcal());
    }

    @Test
    void testUpdateDishProtein() {
        DishDTO dto = dishDAO.createDish(createTestDish("Proteinrig ret"));
        DishDTO updated = dishDAO.updateDishProtein(dto.getId(), 37.5);
        assertEquals(37.5, updated.getProtein());
    }

    @Test
    void testUpdateDishCarbohydrates() {
        DishDTO dto = dishDAO.createDish(createTestDish("Kartoffelret"));
        DishDTO updated = dishDAO.updateDishCarbohydrates(dto.getId(), 92.0);
        assertEquals(92.0, updated.getCarbohydrates());
    }

    @Test
    void testUpdateDishFat() {
        DishDTO dto = dishDAO.createDish(createTestDish("Ret med smør"));
        DishDTO updated = dishDAO.updateDishFat(dto.getId(), 33.3);
        assertEquals(33.3, updated.getFat());
    }

    @Test
    void testUpdateDishAvailableFrom() {
        DishDTO dto = dishDAO.createDish(createTestDish("Tilgængelig fra test"));
        LocalDate newDate = LocalDate.now().plusDays(3);
        DishDTO updated = dishDAO.updateDishAvailableFrom(dto.getId(), newDate);
        assertEquals(newDate, updated.getAvailable_from());
    }

    @Test
    void testUpdateDishAvailableUntil() {
        DishDTO dto = dishDAO.createDish(createTestDish("Tilgængelig til test"));
        LocalDate newDate = LocalDate.now().plusDays(12);
        DishDTO updated = dishDAO.updateDishAvailableUntil(dto.getId(), newDate);
        assertEquals(newDate, updated.getAvailable_until());
    }

    @Test
    void testGetDishesByStatus() {
        dishDAO.createDish(createTestDish("Ratatouille (tilgængelig)"));
        List<DishDTO> filtered = dishDAO.getDishesByStatus(DishStatus.TILGÆNGELIG);
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(d -> d.getStatus() == DishStatus.TILGÆNGELIG));
    }

    @Test
    void testGetDishesByAllergen() {
        dishDAO.createDish(createTestDish("Pastaret med gluten"));
        List<DishDTO> filtered = dishDAO.getDishesByAllergen(Allergens.GLUTEN);
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(d -> d.getAllergens() == Allergens.GLUTEN));
    }

    @Test
    void testGetDishesByStatusAndAllergen() {
        dishDAO.createDish(createTestDish("Linsesalat med glutenfri brød"));
        List<DishDTO> filtered = dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN);
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(d ->
                d.getStatus() == DishStatus.TILGÆNGELIG &&
                        d.getAllergens() == Allergens.GLUTEN));
    }
}
