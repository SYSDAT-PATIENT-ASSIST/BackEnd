package dk.patientassist.test.persistence.dao;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.service.dto.DishDTO;
import dk.patientassist.service.dto.RecipeDTO;
import dk.patientassist.service.dto.IngredientDTO;
import dk.patientassist.persistence.ent.IngredientType;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DishDAO.
 * <p>
 * Exercises CRUD operations, filtering, patching, availability updates,
 * and nested recipe/ingredient functionality.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DishDAOTest {
    private DishDAO dao;

    @BeforeAll
    void init() {
        // Initialize Hibernate in TEST mode (Testcontainers)
        HibernateConfig.init(Mode.TEST);
        dao = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    private void ensureIngredientTypeExists(EntityManager em, String name) {
        em.getTransaction().begin();
        boolean exists = em.createQuery(
                "SELECT COUNT(t) FROM IngredientType t WHERE t.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;

        if (!exists) {
            em.persist(new IngredientType(name));
        }

        em.getTransaction().commit();
    }

    @Test
    void testCreateAndGetById() {
        DishDTO dto = basicDish("TestDish");
        DishDTO created = dao.create(dto);
        assertNotNull(created.getId(), "Created dish ID should not be null");

        Optional<DishDTO> fetched = dao.get(created.getId());
        assertTrue(fetched.isPresent(), "Fetched dish should be present");
        assertEquals("TestDish", fetched.get().getName(), "Fetched dish name should match");
    }

    @Test
    void testGetAll() {
        DishDTO d1 = dao.create(basicDish("Dish1"));
        DishDTO d2 = dao.create(basicDish("Dish2"));

        List<DishDTO> list = dao.getAll();
        assertTrue(list.stream().anyMatch(d -> d.getName().equals("Dish1")), "Should contain Dish1");
        assertTrue(list.stream().anyMatch(d -> d.getName().equals("Dish2")), "Should contain Dish2");
    }

    @Test
    void testUpdate() {
        DishDTO original = dao.create(basicDish("ToUpdate"));
        DishDTO updatedDto = basicDish("UpdatedName");
        updatedDto.setKcal(999);

        DishDTO result = dao.update(original.getId(), updatedDto);
        assertEquals("UpdatedName", result.getName(), "Name should be updated");
        assertEquals(999, result.getKcal(), "Kcal should be updated");
    }

    @Test
    void testDelete() {
        DishDTO dto = dao.create(basicDish("ToDelete"));
        assertTrue(dao.delete(dto.getId()), "Delete should return true");
        assertFalse(dao.get(dto.getId()).isPresent(), "Dish should no longer exist");
    }

    @Test
    void testQueriesByStatusAndAllergen() {
        DishDTO dto = basicDish("FilterDish");
        dto.setStatus(DishStatus.TILGÆNGELIG);
        dto.setAllergens(Set.of(Allergens.GLUTEN));
        DishDTO created = dao.create(dto);

        List<DishDTO> byStatus = dao.getDishesByStatus(DishStatus.TILGÆNGELIG);
        assertTrue(byStatus.stream().anyMatch(d -> d.getId().equals(created.getId())));

        List<DishDTO> byAllergen = dao.getDishesByAllergen(Allergens.GLUTEN);
        assertTrue(byAllergen.stream().anyMatch(d -> d.getId().equals(created.getId())));

        List<DishDTO> combined = dao.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN);
        assertTrue(combined.stream().anyMatch(d -> d.getId().equals(created.getId())));
    }

    @Test
    void testUpdateDishField() {
        DishDTO dto = dao.create(basicDish("PatchDish"));
        Optional<DishDTO> patched = dao.updateDishField(dto.getId(), "name", "Patched");
        assertTrue(patched.isPresent());
        assertEquals("Patched", patched.get().getName());
    }

    @Test
    void testUpdateAvailability() {
        DishDTO dto = dao.create(basicDish("AvailDish"));
        LocalDate from = LocalDate.now().plusDays(2);
        LocalDate until = from.plusDays(3);
        DishDTO updated = dao.updateAvailability(dto.getId(), from, until);
        assertEquals(from, updated.getAvailableFrom());
        assertEquals(until, updated.getAvailableUntil());
    }

    @Test
    void testCreateWithRecipeAndIngredients() {
        EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager();
        ensureIngredientTypeExists(em, "Tomato"); // Ensure case-sensitive match

        RecipeDTO recipe = new RecipeDTO();
        recipe.setTitle("R");
        recipe.setInstructions("I");

        IngredientDTO ing = new IngredientDTO();
        ing.setName("Tomato"); // Must match DB exactly

        recipe.setIngredients(List.of(ing));

        DishDTO dto = basicDish("Full");
        dto.setRecipe(recipe);
        DishDTO created = dao.createWithRecipeAndIngredients(dto);

        assertNotNull(created.getRecipe());
        assertEquals("R", created.getRecipe().getTitle());
        assertFalse(created.getRecipe().getIngredients().isEmpty());
    }

    @Test
    void testUpdateDishRecipeAndAllergens() {
        EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager();
        ensureIngredientTypeExists(em, "Tomato");

        RecipeDTO recipe = new RecipeDTO();
        recipe.setTitle("R");
        recipe.setInstructions("I");

        IngredientDTO ing = new IngredientDTO();
        ing.setName("Tomato");

        recipe.setIngredients(List.of(ing));

        DishDTO dto = basicDish("FullUp");
        dto.setRecipe(recipe);
        dto.setAllergens(Set.of(Allergens.SESAM));
        DishDTO created = dao.createWithRecipeAndIngredients(dto);

        ensureIngredientTypeExists(em, "Citron");

        recipe.setTitle("R2");
        Set<Allergens> newAll = Set.of(Allergens.GLUTEN);
        DishDTO updated = dao.updateDishRecipeAndAllergens(created.getId(), newAll, recipe);

        assertEquals("R2", updated.getRecipe().getTitle());
        assertTrue(updated.getAllergens().contains(Allergens.GLUTEN));
    }

    @Test
    void testMiscQueries() {
        dao.create(basicDish("O1"));
        dao.create(basicDish("O2"));

        dao.getMostOrderedDishes(5);
        dao.getCurrentlyAvailableDishes();
        dao.getAvailableDishesByAllergen(Allergens.LAKTOSE);
    }

    /**
     * Helper to create a basic DishDTO with required fields.
     */
    private DishDTO basicDish(String name) {
        DishDTO dto = new DishDTO();
        dto.setName(name);
        dto.setDescription("D");
        dto.setStatus(DishStatus.TILGÆNGELIG);
        dto.setAvailableFrom(LocalDate.now().minusDays(1));
        dto.setAvailableUntil(LocalDate.now().plusDays(1));
        dto.setKcal(100);
        dto.setProtein(10);
        dto.setCarbohydrates(20);
        dto.setFat(5);
        dto.setAllergens(Set.of());
        return dto;
    }
}
