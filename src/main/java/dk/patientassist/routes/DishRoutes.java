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
                get("/all", controller::getAllDishes, Role.ANYONE);
                post("/new", controller::createNewDish, Role.ANYONE);
                get("/{id}", controller::getDishById, Role.ANYONE);
                get("/filter", controller::getFilteredDishes, Role.ANYONE);
                delete("/{id}", controller::deleteExistingDish, Role.ANYONE);
                post("/full", controller::createDishWithRecipeAndIngredients, Role.ANYONE);
                patch("/{id}/recipe-and-allergens", controller::updateDishRecipeAndAllergens, Role.ANYONE);
                get("/most-ordered", controller::getMostOrderedDishes, Role.ANYONE);
                get("/available", controller::getAvailableDishes);

                // PATCH endpoints
                patch("/{id}/name", controller::updateDishName, Role.ANYONE);
                patch("/{id}/description", controller::updateDishDescription, Role.ANYONE);
                patch("/{id}/kcal", controller::updateDishKcal, Role.ANYONE);
                patch("/{id}/protein", controller::updateDishProtein, Role.ANYONE);
                patch("/{id}/carbohydrates", controller::updateDishCarbohydrates, Role.ANYONE);
                patch("/{id}/fat", controller::updateDishFat, Role.ANYONE);
                patch("/{id}/status", controller::updateDishStatus, Role.ANYONE);
                patch("/{id}/allergens", controller::updateDishAllergens, Role.ANYONE);
                patch("/{id}/available_from", controller::updateDishAvailableFrom, Role.ANYONE);
                patch("/{id}/available_until", controller::updateDishAvailableUntil, Role.ANYONE);
            });
        };
    }
}
