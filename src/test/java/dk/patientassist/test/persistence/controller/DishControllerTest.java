package dk.patientassist.test.persistence.controller;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.IngredientDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DishControllerTest {

    private static final Logger log = LoggerFactory.getLogger(DishControllerTest.class);

    private DishDAO dishDAO;
    private Context ctx;
    private DishController controller;

    @BeforeEach
    void setUp() {
        dishDAO = mock(DishDAO.class);
        ctx = mock(Context.class);
        controller = new DishController(dishDAO);

        when(ctx.status(anyInt())).thenReturn(ctx);
        when(ctx.result(anyString())).thenReturn(ctx);
        when(ctx.json(any())).thenReturn(ctx);

        log.info("Setup complete for DishControllerTest");
    }

    @Test
    void testGetAllDishes() {
        log.info("Running testGetAllDishes()");
        List<DishDTO> mockDishes = List.of(new DishDTO(), new DishDTO());
        when(dishDAO.getAll()).thenReturn(mockDishes);

        controller.getAllDishes(ctx);

        verify(ctx).json(mockDishes);
        log.info("Returned {} dishes", mockDishes.size());
    }

    @Test
    void testGetDishById_found() {
        log.info("Running testGetDishById_found()");
        DishDTO dish = new DishDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.get(1)).thenReturn(Optional.of(dish));

        controller.getDishById(ctx);

        verify(ctx).json(dish);
        log.info("Dish with ID 1 returned successfully");
    }

    @Test
    void testGetDishById_notFound() {
        log.info("Running testGetDishById_notFound()");
        when(ctx.pathParam("id")).thenReturn("999");
        when(dishDAO.get(999)).thenReturn(Optional.empty());

        controller.getDishById(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Not found");
        log.warn("Dish with ID 999 not found");
    }

    @Test
    void testCreateNewDish() {
        log.info("Running testCreateNewDish()");
        DishDTO incoming = new DishDTO();
        DishDTO created = new DishDTO();
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(incoming);
        when(dishDAO.create(incoming)).thenReturn(created);

        controller.createNewDish(ctx);

        verify(ctx).status(201);
        verify(ctx).json(created);
        log.info("New dish created and returned");
    }

    @Test
    void testDeleteExistingDish_success() {
        log.info("Running testDeleteExistingDish_success()");
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenReturn(true);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(200);
        log.info("Dish with ID 1 deleted successfully");
    }

    @Test
    void testDeleteExistingDish_notFound() {
        log.info("Running testDeleteExistingDish_notFound()");
        when(ctx.pathParam("id")).thenReturn("99");
        when(dishDAO.delete(99)).thenReturn(false);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(404);
        log.warn("Attempted to delete non-existing dish with ID 99");
    }

    @Test
    void testGetFilteredDishes_withParams() {
        log.info("Running testGetFilteredDishes_withParams()");
        when(ctx.queryParam("status")).thenReturn("TILGÆNGELIG");
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");

        controller.getFilteredDishes(ctx);

        verify(dishDAO).getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN);
        log.info("Filtered dishes by status TILGÆNGELIG and allergen GLUTEN");
    }

    @Test
    void testGetFilteredDishes_noParams() {
        log.info("Running testGetFilteredDishes_noParams()");
        when(ctx.queryParam("status")).thenReturn(null);
        when(ctx.queryParam("allergen")).thenReturn(null);

        controller.getFilteredDishes(ctx);

        verify(dishDAO).getDishesByStatusAndAllergen(null, null);
        log.info("Filtered dishes with no status or allergen");
    }

    @Test
    void testUpdateDishName_validPatch() {
        log.info("Running testUpdateDishName_validPatch()");
        when(ctx.pathParam("id")).thenReturn("2");
        when(ctx.body()).thenReturn("Frikadeller");

        DishDTO updated = new DishDTO();
        when(dishDAO.updateDishField(2, "name", "Frikadeller")).thenReturn(Optional.of(updated));

        controller.updateDishName(ctx);

        verify(ctx).json(updated);
        log.info("Dish name updated to 'Frikadeller'");
    }

    @Test
    void testUpdateDishName_invalidId() {
        log.info("Running testUpdateDishName_invalidId()");
        when(ctx.pathParam("id")).thenReturn("999");
        when(ctx.body()).thenReturn("Frikadeller");

        when(dishDAO.updateDishField(999, "name", "Frikadeller")).thenReturn(Optional.empty());

        controller.updateDishName(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Dish not found");
        log.warn("Update failed: Dish with ID 999 not found");
    }

    @Test
    void testUpdateDishAllergens_valid() {
        log.info("Running testUpdateDishAllergens_valid()");
        when(ctx.pathParam("id")).thenReturn("1");
        List<String> allergensInput = List.of("gluten", "laktose");
        when(ctx.bodyAsClass(List.class)).thenReturn(allergensInput);

        DishDTO updated = new DishDTO();
        when(dishDAO.updateDishField(eq(1), eq("allergens"), any())).thenReturn(Optional.of(updated));

        controller.updateDishAllergens(ctx);

        verify(ctx).json(updated);
        log.info("Dish allergens updated");
    }

    @Test
    void testUpdateDishAvailability_valid() {
        log.info("Running testUpdateDishAvailability_valid()");
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = new DishDTO();
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(7));

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.updateDishField(eq(1), eq("availableFrom"), any())).thenReturn(Optional.of(dto));
        when(dishDAO.updateDishField(eq(1), eq("availableUntil"), any())).thenReturn(Optional.of(dto));

        controller.updateDishAvailability(ctx);

        verify(ctx).json(dto);
        log.info("Dish availability updated: {} to {}", dto.getAvailableFrom(), dto.getAvailableUntil());
    }

    @Test
    void testCreateDishWithRecipeAndIngredients_valid() {
        log.info("Running testCreateDishWithRecipeAndIngredients_valid()");
        DishDTO dto = new DishDTO();
        dto.setKcal(300);
        dto.setProtein(20);
        dto.setFat(10);
        dto.setCarbohydrates(40);
        dto.setAllergens(Set.of(Allergens.GLUTEN));

        RecipeDTO recipe = new RecipeDTO();
        recipe.setTitle("Frikadelleopskrift");
        recipe.setInstructions("Steg frikadeller på panden.");

        IngredientDTO ingredient = new IngredientDTO();
        ingredient.setName("Hakket svinekød");

        recipe.setIngredients(List.of(ingredient));
        dto.setRecipe(recipe);

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.createWithRecipeAndIngredients(dto)).thenReturn(dto);

        controller.createDishWithRecipeAndIngredients(ctx);

        verify(ctx).status(201);
        verify(ctx).json(dto);
        log.info("Dish with recipe 'Frikadelleopskrift' created successfully");
    }
}
