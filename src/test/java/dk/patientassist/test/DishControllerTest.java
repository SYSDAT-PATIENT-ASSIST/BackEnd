package dk.patientassist.test;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.test.utilities.TestUtils;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishControllerTest {

    @Mock
    private DishDAO dishDAO;

    @Mock
    private Context ctx;

    @InjectMocks
    private DishController controller;

    private DishDTO testDish;
    private TestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils = new TestUtils();
        testDish = testUtils.buildDishDTO("Testret", 500);

        lenient().when(ctx.status(anyInt())).thenReturn(ctx);
        lenient().when(ctx.result(anyString())).thenReturn(ctx);
    }


    @Test
    void createNewDish_shouldPersistAndReturnDish() {
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(testDish);
        when(dishDAO.create(any(DishDTO.class))).thenReturn(testDish);

        controller.createNewDish(ctx);

        verify(ctx).status(201);
        verify(ctx).json(testDish);
    }

    @Test
    void getDishById_shouldReturnDishIfFound() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.get(1)).thenReturn(Optional.of(testDish));

        controller.getDishById(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void getDishById_shouldReturn404IfNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(dishDAO.get(999)).thenReturn(Optional.empty());

        controller.getDishById(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Not found");
    }

    @Test
    void deleteExistingDish_shouldDeleteAndReturn200() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenReturn(true);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(200);
    }

    @Test
    void deleteExistingDish_shouldReturn404IfNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(dishDAO.delete(999)).thenReturn(false);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(404);
    }

    @Test
    void updateDishName_shouldUpdateWithMockedDTO() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("Nyt navn");
        when(dishDAO.updateDishField(1, "name", "Nyt navn")).thenReturn(Optional.of(testDish));

        controller.updateDishName(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishKcal_shouldParseAndUpdateSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("350.0");
        when(dishDAO.updateDishField(1, "kcal", 350.0)).thenReturn(Optional.of(testDish));

        controller.updateDishKcal(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishStatus_shouldUpdateEnumSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("UDSOLGT");
        when(dishDAO.updateDishField(1, "status", DishStatus.UDSOLGT)).thenReturn(Optional.of(testDish));

        controller.updateDishStatus(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishField_shouldReturn400OnInvalidInput() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("not-a-double");

        controller.updateDishKcal(ctx);

        verify(ctx).status(400);
        verify(ctx).result(contains("Invalid input"));
    }

    @Test
    void updateDishField_shouldReturn404IfDishNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(ctx.body()).thenReturn("Testnavn");
        when(dishDAO.updateDishField(999, "name", "Testnavn")).thenReturn(Optional.empty());

        controller.updateDishName(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Dish not found");
    }

    @Test
    void getFilteredDishes_shouldReturnWithBothParams() {
        when(ctx.queryParam("status")).thenReturn("TILGÆNGELIG");
        when(ctx.queryParam("allergen")).thenReturn("ÆG");

        List<DishDTO> result = List.of(testDish);
        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.ÆG)).thenReturn(result);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(result);
    }

    @Test
    void updateDishAvailability_shouldUpdateFromAndUntilSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = testUtils.buildDishDTO("Available Dish", 400);
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(5));

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.updateDishField(1, "availableFrom", dto.getAvailableFrom())).thenReturn(Optional.of(dto));
        when(dishDAO.updateDishField(1, "availableUntil", dto.getAvailableUntil())).thenReturn(Optional.of(dto));

        controller.updateDishAvailability(ctx);

        verify(ctx).json(dto);
    }

    @Test
    void updateDishAllergens_shouldUpdateCorrectly() {
        when(ctx.pathParam("id")).thenReturn("1");
        List<String> input = List.of("ÆG", "GLUTEN");
        Set<Allergens> expected = Set.of(Allergens.ÆG, Allergens.GLUTEN);

        when(ctx.bodyAsClass(List.class)).thenReturn(input);
        when(dishDAO.updateDishField(1, "allergens", expected)).thenReturn(Optional.of(testDish));

        controller.updateDishAllergens(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishRecipeAndAllergens_shouldUpdateAndReturnDish() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = testUtils.buildDishDTO("Ret m. opskrift", 300);
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.updateDishRecipeAndAllergens(1, dto.getAllergens(), dto.getRecipe())).thenReturn(dto);

        controller.updateDishRecipeAndAllergens(ctx);

        verify(ctx).json(dto);
    }

    @Test
    void updateDishRecipeAndAllergens_shouldReturn400IfInvalid() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = new DishDTO();
        dto.setAllergens(Set.of());  // Invalid
        dto.setRecipe(testUtils.buildRecipeDTO("Opskrift", "Step 1", List.of("Salt")));

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);

        controller.updateDishRecipeAndAllergens(ctx);

        verify(ctx).status(400);
        verify(ctx).result(contains("Allergens must not be empty"));
    }
}
