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

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DishController} using Mockito to isolate dependencies.
 */
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
    }

    /** Test that a new dish is created and returned with status 201 */
    @Test
    void createNewDish_shouldPersistAndReturnDish() {
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(testDish);
        when(dishDAO.create(any(DishDTO.class))).thenReturn(testDish);

        controller.createNewDish(ctx);

        verify(ctx).status(201);
        verify(ctx).json(testDish);
    }

    /** Test that a dish is returned when found by ID */
    @Test
    void getDishById_shouldReturnDishIfFound() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.get(1)).thenReturn(Optional.of(testDish));

        controller.getDishById(ctx);

        verify(ctx).json(testDish);
    }

    /** Test that 404 is returned if dish is not found */
    @Test
    void getDishById_shouldReturn404IfNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(dishDAO.get(999)).thenReturn(Optional.empty());

        controller.getDishById(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Not found");
    }

    /** Test that a dish is deleted successfully */
    @Test
    void deleteExistingDish_shouldDeleteAndReturn200() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenReturn(true);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(200);
    }

    /** Test that 404 is returned if trying to delete a non-existent dish */
    @Test
    void deleteExistingDish_shouldReturn404IfNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(dishDAO.delete(999)).thenReturn(false);

        controller.deleteExistingDish(ctx);

        verify(ctx).status(404);
    }

    /** Test updating a dish's name */
    @Test
    void updateDishName_shouldUpdateWithMockedDTO() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("Nyt navn");
        when(dishDAO.updateDishField(eq(1), eq("name"), eq("Nyt navn")))
                .thenReturn(Optional.of(testDish));

        controller.updateDishName(ctx);

        verify(ctx).json(testDish);
    }

    /** Test updating dish kcal with valid input */
    @Test
    void updateDishKcal_shouldParseAndUpdateSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("350.0");
        when(dishDAO.updateDishField(eq(1), eq("kcal"), eq(350.0)))
                .thenReturn(Optional.of(testDish));

        controller.updateDishKcal(ctx);

        verify(ctx).json(testDish);
    }

    /** Test updating dish status enum */
    @Test
    void updateDishStatus_shouldUpdateEnumSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("UDSOLGT");
        when(dishDAO.updateDishField(eq(1), eq("status"), eq(DishStatus.UDSOLGT)))
                .thenReturn(Optional.of(testDish));

        controller.updateDishStatus(ctx);

        verify(ctx).json(testDish);
    }

    /** Test that invalid input returns 400 for kcal update */
    @Test
    void updateDishField_shouldReturn400OnInvalidInput() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("not-a-double");

        controller.updateDishKcal(ctx);

        verify(ctx).status(400);
        verify(ctx).result(contains("Invalid input"));
    }

    /** Test that update returns 404 if dish is not found */
    @Test
    void updateDishField_shouldReturn404IfDishNotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(ctx.body()).thenReturn("Testnavn");
        when(dishDAO.updateDishField(eq(999), eq("name"), eq("Testnavn"))).thenReturn(Optional.empty());

        controller.updateDishName(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Dish not found");
    }

    /** Test filtering by both status and allergen */
    @Test
    void getFilteredDishes_shouldReturnWithBothParams() {
        when(ctx.queryParam("status")).thenReturn("TILGÆNGELIG");
        when(ctx.queryParam("allergen")).thenReturn("ÆG");

        List<DishDTO> result = List.of(testDish);
        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.ÆG))
                .thenReturn(result);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(result);
    }

    /** Test update of availability dates */
    @Test
    void updateDishAvailability_shouldUpdateFromAndUntilSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = testUtils.buildDishDTO("Available Dish", 400); // <-- rettet her
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(5));

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.updateDishField(eq(1), eq("availableFrom"), eq(dto.getAvailableFrom())))
                .thenReturn(Optional.of(dto));
        when(dishDAO.updateDishField(eq(1), eq("availableUntil"), eq(dto.getAvailableUntil())))
                .thenReturn(Optional.of(dto));

        controller.updateDishAvailability(ctx);

        verify(ctx).json(dto);
    }


    /** Test update of allergens with valid input */
    @Test
    void updateDishAllergens_shouldUpdateCorrectly() {
        when(ctx.pathParam("id")).thenReturn("1");
        List<String> input = List.of("ÆG", "GLUTEN");
        Set<Allergens> expected = Set.of(Allergens.ÆG, Allergens.GLUTEN);

        when(ctx.bodyAsClass(List.class)).thenReturn(input);
        when(dishDAO.updateDishField(eq(1), eq("allergens"), eq(expected))).thenReturn(Optional.of(testDish));

        controller.updateDishAllergens(ctx);

        verify(ctx).json(testDish);
    }

    /** Test update of recipe and allergens */
    @Test
    void updateDishRecipeAndAllergens_shouldUpdateAndReturnDish() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = testUtils.buildDishDTO("Ret m. opskrift", 300);
        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);
        when(dishDAO.updateDishRecipeAndAllergens(eq(1), eq(dto.getAllergens()), eq(dto.getRecipe())))
                .thenReturn(dto);

        controller.updateDishRecipeAndAllergens(ctx);

        verify(ctx).json(dto);
    }

    /** Test update of recipe/allergens fails when allergens are empty or missing */
    @Test
    void updateDishRecipeAndAllergens_shouldReturn400IfInvalid() {
        when(ctx.pathParam("id")).thenReturn("1");

        DishDTO dto = new DishDTO();
        dto.setAllergens(Set.of()); // tomt set, undgår null
        dto.setRecipe(testUtils.buildRecipeDTO("Opskrift", "Step 1", List.of("Salt")));

        when(ctx.bodyAsClass(DishDTO.class)).thenReturn(dto);

        controller.updateDishRecipeAndAllergens(ctx);

        verify(ctx).status(400);
        verify(ctx).result(contains("Allergens must not be empty")); // eller tilpas efter faktisk fejltekst
    }

}
