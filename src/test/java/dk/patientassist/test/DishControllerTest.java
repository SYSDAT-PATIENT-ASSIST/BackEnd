package dk.patientassist.test;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setUp() {
        testDish = new DishDTO(
                "Frikadeller", "Med sovs",
                LocalDate.now(), LocalDate.now().plusDays(5),
                DishStatus.TILGÆNGELIG, 500, 25, 30, 20, Allergens.ÆG
        );
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
        when(dishDAO.updateDishField(1, "name", "Nyt navn"))
                .thenReturn(Optional.of(testDish));

        controller.updateDishName(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishKcal_shouldParseAndUpdateSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("350.0");
        when(dishDAO.updateDishField(1, "kcal", 350.0))
                .thenReturn(Optional.of(testDish));

        controller.updateDishKcal(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishStatus_shouldUpdateEnumSuccessfully() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("UDSOLGT");
        when(dishDAO.updateDishField(1, "status", DishStatus.UDSOLGT))
                .thenReturn(Optional.of(testDish));

        controller.updateDishStatus(ctx);

        verify(ctx).json(testDish);
    }

    @Test
    void updateDishField_shouldReturn400OnInvalidInput() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("not-a-double");

        doThrow(new NumberFormatException("Forventede tal"))
                .when(dishDAO).updateDishField(anyInt(), eq("kcal"), any());

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
        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.ÆG))
                .thenReturn(result);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(result);
    }

    @Test
    void getFilteredDishes_shouldReturnWithOnlyStatus() {
        when(ctx.queryParam("status")).thenReturn("TILGÆNGELIG");
        when(ctx.queryParam("allergen")).thenReturn(null);

        List<DishDTO> result = List.of(testDish);
        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, null))
                .thenReturn(result);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(result);
    }

    @Test
    void getFilteredDishes_shouldReturnWithOnlyAllergen() {
        when(ctx.queryParam("status")).thenReturn(null);
        when(ctx.queryParam("allergen")).thenReturn("GLUTEN");

        List<DishDTO> result = List.of(testDish);
        when(dishDAO.getDishesByStatusAndAllergen(null, Allergens.GLUTEN))
                .thenReturn(result);

        controller.getFilteredDishes(ctx);

        verify(ctx).json(result);
    }

    @Test
    void getFilteredDishes_shouldReturnEmptyIfNothingFound() {
        when(ctx.queryParam("status")).thenReturn("UDSOLGT");
        when(ctx.queryParam("allergen")).thenReturn("SESAM");

        when(dishDAO.getDishesByStatusAndAllergen(DishStatus.UDSOLGT, Allergens.SESAM))
                .thenReturn(List.of());

        controller.getFilteredDishes(ctx);

        verify(ctx).json(List.of());
    }

    @Test
    void deleteExistingDish_shouldHandleInvalidIdFormat() {
        when(ctx.pathParam("id")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> controller.deleteExistingDish(ctx));
    }

    @Test
    void deleteExistingDish_shouldPropagateExceptionFromDAO() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(dishDAO.delete(1)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> controller.deleteExistingDish(ctx));
    }

    @Test
    void updateDishName_shouldUpdateWithRealDTO() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("Ny ret");

        DishDTO updated = new DishDTO("Ny ret", "desc", LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 200, 10, 20, 5, Allergens.GLUTEN);
        when(dishDAO.updateDishField(1, "name", "Ny ret")).thenReturn(Optional.of(updated));

        controller.updateDishName(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishName_shouldReturn404WhenDishNotFound_again() throws Exception {
        when(ctx.pathParam("id")).thenReturn("999");
        when(ctx.body()).thenReturn("Nonexistent");
        when(dishDAO.updateDishField(999, "name", "Nonexistent")).thenReturn(Optional.empty());

        controller.updateDishName(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Dish not found");
    }

    @Test
    void updateDishName_shouldReturn400OnInvalidInput_again() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("");

        when(dishDAO.updateDishField(1, "name", "")).thenThrow(new IllegalArgumentException("Invalid input"));

        controller.updateDishName(ctx);

        verify(ctx).status(400);
        verify(ctx).result(startsWith("Invalid input"));
    }

    @Test
    void updateDishName_shouldThrowIfIdIsInvalid_again() {
        when(ctx.pathParam("id")).thenReturn("notANumber");

        assertThrows(NumberFormatException.class, () -> controller.updateDishName(ctx));
    }

    @Test
    void updateDishKcal_shouldUpdateSuccessfully_withRealDTO() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("450.5");

        DishDTO updated = new DishDTO("Ret", "desc", LocalDate.now(), LocalDate.now().plusDays(1),
                DishStatus.TILGÆNGELIG, 450.5, 10, 20, 5, Allergens.GLUTEN);
        when(dishDAO.updateDishField(1, "kcal", 450.5)).thenReturn(Optional.of(updated));

        controller.updateDishKcal(ctx);

        verify(ctx).json(updated);
    }

    @Test
    void updateDishKcal_shouldReturn400ForInvalidDouble_again() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.body()).thenReturn("notANumber");

        controller.updateDishKcal(ctx);

        verify(ctx).status(400);
        verify(ctx).result(startsWith("Invalid input"));
    }

    @Test
    void updateDishKcal_shouldReturn404IfDishNotFound_again() throws Exception {
        when(ctx.pathParam("id")).thenReturn("123");
        when(ctx.body()).thenReturn("300.0");
        when(dishDAO.updateDishField(123, "kcal", 300.0)).thenReturn(Optional.empty());

        controller.updateDishKcal(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Dish not found");
    }
}
