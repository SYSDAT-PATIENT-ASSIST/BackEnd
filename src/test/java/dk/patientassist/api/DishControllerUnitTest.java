package dk.patientassist.api;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.Javalin;
import io.javalin.core.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DishController} endpoints.
 */
class DishControllerUnitTest {

    private DishDAO mockDAO;
    private DishDTO sampleDish;

    @BeforeEach
    void setUp() {
        mockDAO = mock(DishDAO.class);
        sampleDish = new DishDTO(
                "Testret",
                "En lækker testret",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 10),
                DishStatus.TILGÆNGELIG,
                500.0,
                25.0,
                30.0,
                15.0,
                Allergens.GLUTEN
        );
        sampleDish.setId(42);
    }

    @Test
    void testPostDish() {
        when(mockDAO.create(any())).thenReturn(sampleDish);
        DishController controller = new DishController(mockDAO);

        Javalin app = Javalin.create(config -> controller.registerRoutes(app));

        TestUtil.test(app, (client) -> {
            var response = client.post("/api/dishes")
                    .body("""
                          {
                            "name": "Testret",
                            "description": "En lækker testret",
                            "availableFrom": "2025-06-01",
                            "availableUntil": "2025-06-10",
                            "status": "TILGÆNGELIG",
                            "kcal": 500.0,
                            "protein": 25.0,
                            "carbohydrates": 30.0,
                            "fat": 15.0,
                            "allergens": "GLUTEN"
                          }
                          """)
                    .asString();

            assertEquals(201, response.code());
            assertTrue(response.body().contains("\"id\":42"));
        });
    }

    @Test
    void testGetDishById() {
        when(mockDAO.get(42)).thenReturn(Optional.of(sampleDish));
        DishController controller = new DishController(mockDAO);

        Javalin app = Javalin.create(config -> app.get("/api/dishes/{id}", controller::getDishById));

        TestUtil.test(app, (client) -> {
            var response = client.get("/api/dishes/42").asString();
            assertEquals(200, response.code());
            assertTrue(response.body().contains("Testret"));
        });
    }

    @Test
    void testDeleteDish() {
        when(mockDAO.delete(42)).thenReturn(true);
        DishController controller = new DishController(mockDAO);

        Javalin app = Javalin.create(config -> app.delete("/api/dishes/{id}", controller::deleteExistingDish));

        TestUtil.test(app, (client) -> {
            var response = client.delete("/api/dishes/42").asString();
            assertEquals(200, response.code());
        });
    }

    @Test
    void testPatchDishKcal() {
        DishDTO updatedDish = new DishDTO(sampleDish);
        updatedDish.setKcal(777.0);
        when(mockDAO.updateDishField(eq(42), eq("kcal"), any())).thenReturn(Optional.of(updatedDish));

        DishController controller = new DishController(mockDAO);
        Javalin app = Javalin.create(config -> app.patch("/api/dishes/{id}/kcal", controller::updateDishKcal));

        TestUtil.test(app, (client) -> {
            var response = client.patch("/api/dishes/42/kcal")
                    .body("777.0")
                    .asString();

            assertEquals(200, response.code());
            assertTrue(response.body().contains("777.0"));
        });
    }

    @Test
    void testGetFilteredDishes() {
        when(mockDAO.getDishesByStatusAndAllergen(DishStatus.TILGÆNGELIG, Allergens.GLUTEN))
                .thenReturn(List.of(sampleDish));

        DishController controller = new DishController(mockDAO);
        Javalin app = Javalin.create(config -> app.get("/api/dishes/filter", controller::getFilteredDishes));

        TestUtil.test(app, (client) -> {
            var response = client.get("/api/dishes/filter?status=TILGÆNGELIG&allergen=GLUTEN").asString();
            assertEquals(200, response.code());
            assertTrue(response.body().contains("Testret"));
        });
    }

    @Test
    void testPatchDishName() {
        DishDTO updated = new DishDTO(sampleDish);
        updated.setName("Ny ret");
        when(mockDAO.updateDishField(eq(42), eq("name"), eq("Ny ret"))).thenReturn(Optional.of(updated));

        DishController controller = new DishController(mockDAO);
        Javalin app = Javalin.create(config -> app.patch("/api/dishes/{id}/name", controller::updateDishName));

        TestUtil.test(app, (client) -> {
            var response = client.patch("/api/dishes/42/name")
                    .body("Ny ret")
                    .asString();

            assertEquals(200, response.code());
            assertTrue(response.body().contains("Ny ret"));
        });
    }

    @Test
    void testPatchDishDescription() {
        DishDTO updated = new DishDTO(sampleDish);
        updated.setDescription("Opdateret beskrivelse");
        when(mockDAO.updateDishField(eq(42), eq("description"), eq("Opdateret beskrivelse"))).thenReturn(Optional.of(updated));

        DishController controller = new DishController(mockDAO);
        Javalin app = Javalin.create(config -> app.patch("/api/dishes/{id}/description", controller::updateDishDescription));

        TestUtil.test(app, (client) -> {
            var response = client.patch("/api/dishes/42/description")
                    .body("Opdateret beskrivelse")
                    .asString();

            assertEquals(200, response.code());
            assertTrue(response.body().contains("Opdateret beskrivelse"));
        });
    }

}
