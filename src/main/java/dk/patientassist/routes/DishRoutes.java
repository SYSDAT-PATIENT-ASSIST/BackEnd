package dk.patientassist.routes;

import dk.patientassist.control.DishController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Defines REST endpoints for managing dishes, including create, read, update, delete operations.
 * Access to routes is restricted by user roles using Javalin's role-based system.
 */
public class DishRoutes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishRoutes.class);
    private static final DishController controller = new DishController();

    public static EndpointGroup getDishRoutes() {
        return () -> path("/api/dishes", () -> {

            LOGGER.info("Registering Dish API routes");

            // --- Public GET endpoints ---
            get(controller::getAllDishes, Role.ANYONE); // fallback to /api/dishes
            get("/all", controller::getAllDishes, Role.ANYONE);
            get("/{id}", controller::getDishById, Role.ANYONE);
            get("/filter", controller::getFilteredDishes, Role.ANYONE);
            get("/available", controller::getAvailableDishes, Role.ANYONE);
            get("/most-ordered", controller::getMostOrderedDishes, Role.ANYONE);

            // --- Create dish (requires kitchen staff or head chef) ---
            post("/new", controller::createDishWithRecipeAndIngredients, Role.KØKKENPERSONALE, Role.HOVEDKOK);

            // --- Delete dish (restricted to head chef or admin) ---
            delete("/{id}", controller::deleteExistingDish, Role.HOVEDKOK, Role.ADMIN);

            // --- PATCH endpoints for updating individual fields ---
            patch("/{id}/name", controller::updateDishName, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/description", controller::updateDishDescription, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/kcal", controller::updateDishKcal, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/protein", controller::updateDishProtein, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/carbohydrates", controller::updateDishCarbohydrates, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/fat", controller::updateDishFat, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/status", controller::updateDishStatus, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/allergens", controller::updateDishAllergens, Role.KØKKENPERSONALE, Role.HOVEDKOK);
            patch("/{id}/available_from", controller::updateDishAvailableFrom, Role.HOVEDKOK);
            patch("/{id}/available_until", controller::updateDishAvailableUntil, Role.HOVEDKOK);
            patch("/{id}/recipe-and-allergens", controller::updateDishRecipeAndAllergens, Role.KØKKENPERSONALE, Role.HOVEDKOK);

            LOGGER.info("Dish API routes registered successfully");
        });
    }
}
