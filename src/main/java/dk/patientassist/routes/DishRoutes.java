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
            post("/new", controller::createDishWithRecipeAndIngredients, Role.KITCHEN_STAFF, Role.HEAD_CHEF);

            // --- DELETE endpoint (only HEAD_CHEF or ADMIN can delete) ---
            delete("/{id}", controller::deleteExistingDish, Role.HEAD_CHEF, Role.ADMIN);

            // --- PATCH endpoints for updating dish fields ---
            patch("/{id}/name", controller::updateDishName, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/description", controller::updateDishDescription, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/kcal", controller::updateDishKcal, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/protein", controller::updateDishProtein, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/carbohydrates", controller::updateDishCarbohydrates, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/fat", controller::updateDishFat, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/status", controller::updateDishStatus, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/allergens", controller::updateDishAllergens, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
            patch("/{id}/available_from", controller::updateDishAvailableFrom, Role.HEAD_CHEF);
            patch("/{id}/available_until", controller::updateDishAvailableUntil, Role.HEAD_CHEF);
            patch("/{id}/recipe-and-allergens", controller::updateDishRecipeAndAllergens, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
        });
    }
}
