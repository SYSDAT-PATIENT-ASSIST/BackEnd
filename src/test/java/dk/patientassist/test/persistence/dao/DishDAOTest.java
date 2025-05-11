package dk.patientassist.test.persistence.dao;

import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishDAOTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAOTest.class);

    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private DishDAO dishDAO;

    @BeforeEach
    void setUp() {
        emf = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(entityManager);

        dishDAO = DishDAO.getInstance(emf);
        dishDAO.setEntityManager(entityManager);
        LOGGER.info("Initialized DishDAO test with mocked EntityManager");
    }

    private DishDTO createValidDishDTO() {
        DishDTO dto = new DishDTO();
        dto.setName("Sample Dish");
        dto.setDescription("A valid dish.");
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(5));
        dto.setStatus(DishStatus.TILGÆNGELIG);
        dto.setKcal(200);
        dto.setProtein(15);
        dto.setCarbohydrates(25);
        dto.setFat(5);
        dto.setAllergens(Set.of());
        LOGGER.debug("Created valid DishDTO: {}", dto);
        return dto;
    }

    @Test
    void create_shouldPersistDishAndReturnDTO() {
        LOGGER.info("Test: create_shouldPersistDishAndReturnDTO");
        DishDTO dto = createValidDishDTO();

        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        DishDTO result = dishDAO.create(dto);

        assertNotNull(result);
        verify(entityManager).persist(any(Dish.class));
        assertEquals("Sample Dish", result.getName());
        LOGGER.info("Dish created: {}", result.getName());
    }

    @Test
    void update_shouldMergeFieldsAndReturnUpdatedDTO() {
        LOGGER.info("Test: update_shouldMergeFieldsAndReturnUpdatedDTO");
        DishDTO dto = createValidDishDTO();
        Dish dishEntity = new Dish(dto);

        when(entityManager.find(Dish.class, 1)).thenReturn(dishEntity);
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));
        when(entityManager.merge(any(Dish.class))).thenReturn(dishEntity);

        DishDTO updated = dishDAO.update(1, dto);

        assertNotNull(updated);
        assertEquals("Sample Dish", updated.getName());
        LOGGER.info("Updated dish: {}", updated);
    }

    @Test
    void get_shouldReturnDishDTOIfFound() {
        LOGGER.info("Test: get_shouldReturnDishDTOIfFound");
        DishDTO dto = createValidDishDTO();
        Dish dishEntity = new Dish(dto);

        when(entityManager.find(Dish.class, 1)).thenReturn(dishEntity);

        Optional<DishDTO> result = dishDAO.get(1);

        assertTrue(result.isPresent());
        assertEquals("Sample Dish", result.get().getName());
        LOGGER.info("Retrieved dish by ID 1: {}", result.get());
    }

    @Test
    void update_shouldReturnNullIfDishNotFound() {
        LOGGER.info("Test: update_shouldReturnNullIfDishNotFound");
        when(entityManager.find(Dish.class, 999)).thenReturn(null);

        DishDTO result = dishDAO.update(999, new DishDTO());

        assertNull(result);
        LOGGER.warn("Dish not found for update with ID: 999");
    }

    @Test
    void delete_shouldRemoveEntityIfFound() {
        LOGGER.info("Test: delete_shouldRemoveEntityIfFound");
        Dish dish = new Dish();
        when(entityManager.find(Dish.class, 1)).thenReturn(dish);
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        boolean deleted = dishDAO.delete(1);

        assertTrue(deleted);
        verify(entityManager).remove(dish);
        LOGGER.info("Deleted dish with ID 1");
    }

    @Test
    void delete_shouldReturnFalseIfNotFound() {
        LOGGER.info("Test: delete_shouldReturnFalseIfNotFound");
        when(entityManager.find(Dish.class, 999)).thenReturn(null);

        boolean deleted = dishDAO.delete(999);

        assertFalse(deleted);
        LOGGER.warn("Attempted to delete non-existing dish with ID 999");
    }

    @Test
    void getDishesByStatus_shouldReturnListOfDishes() {
        LOGGER.info("Test: getDishesByStatus_shouldReturnListOfDishes");
        TypedQuery<DishDTO> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(DishDTO.class))).thenReturn(query);
        when(query.setParameter(eq("status"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new DishDTO()));

        List<DishDTO> dishes = dishDAO.getDishesByStatus(DishStatus.TILGÆNGELIG);

        assertEquals(1, dishes.size());
        LOGGER.info("Found {} dishes with status {}", dishes.size(), DishStatus.TILGÆNGELIG);
    }

    @Test
    void updateDishField_shouldHandleNameUpdate() {
        LOGGER.info("Test: updateDishField_shouldHandleNameUpdate");
        Dish dish = new Dish();
        when(entityManager.find(Dish.class, 1)).thenReturn(dish);
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        Optional<DishDTO> result = dishDAO.updateDishField(1, "name", "New Name");

        assertTrue(result.isPresent());
        assertEquals("New Name", dish.getName());
        LOGGER.info("Successfully updated dish name to '{}'", dish.getName());
    }

    @Test
    void updateDishField_shouldThrowForInvalidField() {
        LOGGER.info("Test: updateDishField_shouldThrowForInvalidField");
        Dish dish = new Dish();
        when(entityManager.find(Dish.class, 1)).thenReturn(dish);
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        assertThrows(IllegalArgumentException.class,
                () -> dishDAO.updateDishField(1, "invalidField", "value"));
        LOGGER.warn("Expected exception thrown for invalid field update");
    }

    @Test
    void createWithRecipeAndIngredients_shouldPersistAndReturnDTO() {
        LOGGER.info("Test: createWithRecipeAndIngredients_shouldPersistAndReturnDTO");
        DishDTO dto = createValidDishDTO();
        RecipeDTO recipe = new RecipeDTO("Title", "Instructions", List.of());
        dto.setRecipe(recipe);
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        DishDTO result = dishDAO.createWithRecipeAndIngredients(dto);

        assertNotNull(result);
        verify(entityManager).persist(any(Dish.class));
        LOGGER.info("Dish with recipe created: {}", result.getName());
    }
}
