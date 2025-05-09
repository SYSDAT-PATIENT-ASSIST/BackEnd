package dk.patientassist.routes;

import dk.patientassist.control.DishController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class DishRoutes {

    private static final DishController controller = new DishController();

    public static EndpointGroup getDishRoutes() {
        return () -> {
            path("/api/dishes", () -> {
                // --- GET ---
                get(controller::getAllDishes, Role.ANYONE);
                get("/all", controller::getAllDishes, Role.ANYONE);
                get("/{id}", controller::getDishById, Role.ANYONE);
                get("/filter", controller::getFilteredDishes, Role.ANYONE);
                get("/available", controller::getAvailableDishes, Role.ANYONE);
                get("/most-ordered", controller::getMostOrderedDishes, Role.ANYONE);

                // --- CREATE ---
                post("/new", controller::createNewDish, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                post("/full", controller::createDishWithRecipeAndIngredients, Role.KITCHEN_STAFF, Role.HEAD_CHEF);

                // --- DELETE ---
                delete("/{id}", controller::deleteExistingDish, Role.HEAD_CHEF, Role.ADMIN);

                // --- PATCH (field updates) ---
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
        };
    }
}
