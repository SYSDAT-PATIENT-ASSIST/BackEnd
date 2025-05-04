package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DishDAOTest {

    private static DishDAO dao;

    @BeforeAll
    static void setupClass() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        dao = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    @BeforeEach
    void cleanUp() {
        dao.getAllAvailableDishes().forEach(d -> dao.deleteDish(d.getId()));
    }

    @Test
    void testCreateDish() {
        DishDTO dto = createTestDishDTO("Boller i karry");
        DishDTO saved = dao.createDish(dto);

        assertNotNull(saved.getId());
        assertEquals(dto.getName(), saved.getName());
    }

    @Test
    void testGetDishById() {
        DishDTO dto = dao.createDish(createTestDishDTO("Frikadeller"));
        DishDTO fetched = dao.getDish(dto.getId());

        assertNotNull(fetched);
        assertEquals("Frikadeller", fetched.getName());
    }

    @Test
    void testUpdateDish() {
        DishDTO original = dao.createDish(createTestDishDTO("Rugbrød"));
        DishDTO updatedDTO = createTestDishDTO("Smørrebrød");
        updatedDTO = new DishDTO(
                "Smørrebrød",
                "Updated description",
                original.getAvailable_from(),
                original.getAvailable_until(),
                DishStatus.UDSOLGT,
                500, 30, 40, 15,
                Allergens.ÆG
        );

        DishDTO updated = dao.updateDish(original.getId(), updatedDTO);

        assertEquals("Smørrebrød", updated.getName());
        assertEquals(DishStatus.UDSOLGT, updated.getStatus());
    }

    @Test
    void testDeleteDish() {
        DishDTO dto = dao.createDish(createTestDishDTO("Pasta Carbonara"));
        DishDTO deleted = dao.deleteDish(dto.getId());

        assertNotNull(deleted);
        assertEquals(dto.getId(), deleted.getId());

        assertNull(dao.getDish(dto.getId()));
    }

    @Test
    void testGetAllAvailableDishes() {
        dao.createDish(createTestDishDTO("Lasagne"));
        dao.createDish(createTestDishDTO("Tacos"));

        List<DishDTO> dishes = dao.getAllAvailableDishes();
        assertTrue(dishes.size() >= 2);
    }

    @Test
    void testFindByName() {
        dao.createDish(createTestDishDTO("Pizza"));
        DishDTO found = dao.findByName("Pizza");

        assertNotNull(found);
        assertEquals("Pizza", found.getName());
    }

    // Utility method
    private DishDTO createTestDishDTO(String name) {
        return new DishDTO(
                name,
                "Test description",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                DishStatus.TILGÆNGELIG,
                600.0,
                20.0,
                50.0,
                10.0,
                Allergens.GLUTEN
        );
    }
}
