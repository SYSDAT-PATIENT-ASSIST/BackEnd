package dk.patientassist.routes;

import dk.patientassist.control.DishController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Defines all RESTful endpoints related to dish management.
 * These include endpoints for retrieving, creating, updating, and deleting dishes.
 * Routes are secured based on user roles using Javalin's role-based access control.
 */
public class DishRoutes {

    private static final DishController controller = new DishController();

    /**
     * Returns an {@link EndpointGroup} containing all dish-related routes.
     *
     * @return the configured endpoint group for dish routes
     */
    public static EndpointGroup getDishRoutes() {
        return () -> path("/api/dishes", () -> {

            // --- GET endpoints (public access) ---
            get(controller::getAllDishes, Role.ANYONE);
            get("/all", controller::getAllDishes, Role.ANYONE);
            get("/{id}", controller::getDishById, Role.ANYONE);
            get("/filter", controller::getFilteredDishes, Role.ANYONE);
            get("/available", controller::getAvailableDishes, Role.ANYONE);
            get("/most-ordered", controller::getMostOrderedDishes, Role.ANYONE);

            // --- POST endpoints (for creating dishes) ---
            post("/new", controller::createDishWithRecipeAndIngredients, Role.KØKKENPERSONALE, Role.HOVEDKOK);

            // --- DELETE endpoint (only HEAD_CHEF or ADMIN can delete) ---
            delete("/{id}", controller::deleteExistingDish, Role.HOVEDKOK, Role.ADMIN);

            // --- PATCH endpoints for updating dish fields ---
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
        });
    }
}
