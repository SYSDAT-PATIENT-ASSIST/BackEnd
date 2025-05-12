package dk.patientassist.test.controller;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.dto.IngredientDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DishControllerTest {
    private DishDAO dishDAO;
    private DishController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        dishDAO = mock(DishDAO.class);
        controller = new DishController(dishDAO);
        ctx = mock(Context.class);

        // Return ctx on status so chaining works
        when(ctx.status(anyInt())).thenAnswer(invocation -> {
            int status = invocation.getArgument(0);
            System.out.println("[ctx.status] → " + status);
            return ctx;
        });

        // Log any .json calls
        doAnswer(invocation -> {
            Object body = invocation.getArgument(0);
            System.out.println("[ctx.json] → " + body);
            return null;
        }).when(ctx).json(any());

        // Log any .result calls
        doAnswer(invocation -> {
            Object text = invocation.getArgument(0);
            System.out.println("[ctx.result] → " + text);
            return null;
        }).when(ctx).result(anyString());
    }

    @Test
    void getAllDishes() {
        List<DishDTO> dishes = Arrays.asList(new DishDTO(), new DishDTO());
        when(dishDAO.getAll()).thenReturn(dishes);

        controller.getAllDishes(ctx);

        verify(ctx).json(dishes);
    }

    @Test
    void createNewDish() {
        DishDTO input = new DishDTO();
        input.setName("TestDish");
        DishDTO created = new DishDTO();
        created.setName("TestDish");
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(input);
        when(dishDAO.create(input)).thenReturn(created);

        controller.createNewDish(ctx);

        verify(ctx).status(201);
        verify(ctx).json(created);
    }

    @Test
    void getDishById_found() {
        DishDTO dish = new DishDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.get(1)).thenReturn(Optional.of(dish));

        controller.getDishById(ctx);

        verify(ctx).json(dish);
    }

    @Test
    void getDishById_notFound() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.get(1)).thenReturn(Optional.empty());

        controller.getDishById(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Not found");
    }

    @Test
    void deleteExistingDish_deleted() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenReturn(true);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(200);
    }

    @Test
    void deleteExistingDish_notDeleted() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenReturn(false);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(404);
    }

    @Test
    void getFilteredDishes_noParams() {
        when(ctx.queryParam("status")).thenReturn(null);
        when(ctx.queryParam("allergen")).thenReturn(null);
        List<DishDTO> dishes = Collections.singletonList(new DishDTO());
        when(dishDAO.getAll()).thenReturn(dishes);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(dishes);
    }

    @Test
    void getFilteredDishes_withStatus() {
        String statusValue = DishStatus.TILGÆNGELIG.toValue();
        when(ctx.queryParam("status")).thenReturn(statusValue);
        when(ctx.queryParam("allergen")).thenReturn(null);
        List<DishDTO> dishes = Collections.singletonList(new DishDTO());
        when(dishDAO.getDishesByStatus(DishStatus.TILGÆNGELIG)).thenReturn(dishes);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(dishes);
    }

    @Test
    void getFilteredDishes_withAllergen() {
        when(ctx.queryParam("status")).thenReturn(null);
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");
        List<DishDTO> dishes = Collections.singletonList(new DishDTO());
        when(dishDAO.getDishesByAllergen(Allergens.GLUTEN)).thenReturn(dishes);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(dishes);
    }

    @Test
    void getFilteredDishes_withStatusAndAllergen() {
        String statusValue = DishStatus.TILGÆNGELIG.toValue();
        when(ctx.queryParam("status")).thenReturn(statusValue);
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");
        List<DishDTO> dishes = Collections.singletonList(new DishDTO());
        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN)).thenReturn(dishes);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(dishes);
    }

    @Test
    void getFilteredDishes_invalidParam() {
        when(ctx.queryParam("status")).thenReturn("BAD");
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");

        controller.getFilteredDishes(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Invalid status or allergen");
    }

    @Test
    void updateDishName_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("NewName");
        DishDTO updated = new DishDTO();
        updated.setName("NewName");
        when(dishDAO.updateDishField(1, "name", "NewName")).thenReturn(Optional.of(updated));

        controller.updateDishName(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishName_notFound() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("NewName");
        when(dishDAO.updateDishField(1, "name", "NewName")).thenReturn(Optional.empty());

        controller.updateDishName(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Not found");
    }

    @Test
    void updateDishKcal_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("200.5");
        DishDTO updated = new DishDTO();
        updated.setKcal(200.5);
        when(dishDAO.updateDishField(1, "kcal", 200.5)).thenReturn(Optional.of(updated));

        controller.updateDishKcal(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishStatus_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        String statusValue = DishStatus.TILGÆNGELIG.toValue();
        when(ctx.body()).thenReturn(statusValue);
        DishDTO updated = new DishDTO();
        updated.setStatus(DishStatus.TILGÆNGELIG);
        when(dishDAO.updateDishField(1, "status", DishStatus.TILGÆNGELIG)).thenReturn(Optional.of(updated));

        controller.updateDishStatus(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishAvailableFrom_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("2025-05-01");
        DishDTO updated = new DishDTO();
        LocalDate date = LocalDate.parse("2025-05-01");
        updated.setAvailableFrom(date);
        when(dishDAO.updateDishField(1, "availableFrom", date)).thenReturn(Optional.of(updated));

        controller.updateDishAvailableFrom(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishAllergens_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        List<String> list = Collections.singletonList("GLUTEN");
        when(ctx.bodyAsClass(List.class)).thenReturn(list);
        Set<Allergens> set = Collections.singleton(Allergens.GLUTEN);
        DishDTO updated = new DishDTO();
        when(dishDAO.updateDishField(1, "allergens", set)).thenReturn(Optional.of(updated));

        controller.updateDishAllergens(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishAvailability_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        DishDTO input = new DishDTO();
        input.setAvailableFrom(LocalDate.of(2025, 5, 1));
        input.setAvailableUntil(LocalDate.of(2025, 5, 10));
        DishDTO updated = new DishDTO();
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(input);
        when(dishDAO.updateAvailability(1, input.getAvailableFrom(), input.getAvailableUntil())).thenReturn(updated);

        controller.updateDishAvailability(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishAvailability_missingDates() {
        when(ctx.pathParam("id")).thenReturn("1");
        DishDTO input = new DishDTO();
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(input);

        controller.updateDishAvailability(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Both availableFrom and availableUntil are required.");
    }

    @Test
    void createDishWithRecipeAndIngredients_success() {
        DishDTO input = new DishDTO();
        input.setName("Dish");
        input.setAllergens(Collections.singleton(Allergens.GLUTEN));
        RecipeDTO recipe = new RecipeDTO();
        recipe.setTitle("Title");
        recipe.setInstructions("Instructions");
        IngredientDTO ing = new IngredientDTO();
        ing.setName("Ing");
        recipe.setIngredients(Collections.singletonList(ing));
        input.setRecipe(recipe);
        DishDTO created = new DishDTO();
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(input);
        when(dishDAO.createWithRecipeAndIngredients(input)).thenReturn(created);

        controller.createDishWithRecipeAndIngredients(ctx);

        verify(ctx).status(201);
        verify(ctx).json(created);
    }

    @Test
    void updateDishRecipeAndAllergens_success() {
        when(ctx.pathParam("id")).thenReturn("1");
        DishDTO input = new DishDTO();
        Set<Allergens> allergens = Collections.singleton(Allergens.SESAM);
        input.setAllergens(allergens);
        RecipeDTO recipe = new RecipeDTO();
        recipe.setTitle("Title");
        recipe.setInstructions("Instr");
        IngredientDTO ing = new IngredientDTO();
        ing.setName("Ing");
        recipe.setIngredients(Collections.singletonList(ing));
        input.setRecipe(recipe);
        DishDTO updated = new DishDTO();
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(input);
        when(dishDAO.updateDishRecipeAndAllergens(1, allergens, recipe)).thenReturn(updated);

        controller.updateDishRecipeAndAllergens(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void getMostOrderedDishes_customLimit() {
        when(ctx.queryParam("limit")).thenReturn("3");
        List<DishDTO> list = Arrays.asList(new DishDTO(), new DishDTO(), new DishDTO());
        when(dishDAO.getMostOrderedDishes(3)).thenReturn(list);

        controller.getMostOrderedDishes(ctx);

        verify(ctx).json(list);
    }

    @Test
    void getMostOrderedDishes_defaultLimit() {
        when(ctx.queryParam("limit")).thenReturn(null);
        List<DishDTO> list = Collections.singletonList(new DishDTO());
        when(dishDAO.getMostOrderedDishes(5)).thenReturn(list);

        controller.getMostOrderedDishes(ctx);

        verify(ctx).json(list);
    }

    @Test
    void getAvailableDishes_noAllergen() {
        when(ctx.queryParam("allergen")).thenReturn(null);
        List<DishDTO> list = Collections.singletonList(new DishDTO());
        when(dishDAO.getCurrentlyAvailableDishes()).thenReturn(list);

        controller.getAvailableDishes(ctx);

        verify(ctx).json(list);
    }

    @Test
    void getAvailableDishes_withAllergen() {
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");
        List<DishDTO> list = Collections.singletonList(new DishDTO());
        when(dishDAO.getAvailableDishesByAllergen(Allergens.GLUTEN)).thenReturn(list);

        controller.getAvailableDishes(ctx);

        verify(ctx).json(list);
    }

    @Test
    void getAvailableDishes_invalidAllergen() {
        when(ctx.queryParam("allergen")).thenReturn("BAD");

        controller.getAvailableDishes(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Invalid allergen");
    }
}