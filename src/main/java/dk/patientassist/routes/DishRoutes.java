package dk.patientassist.routes;

import dk.patientassist.control.DishController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Defines all HTTP endpoints for Dish resources under /api/dishes.
 * <p>
 * Supports creation, retrieval, update (full & partial), deletion,
 * and specialized queries (availability, popularity).
 * Access is secured via role-based restrictions.
 */
public class DishRoutes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishRoutes.class);
    private static final DishController CONTROLLER = new DishController();

    /**
     * Registers the Dish API routes.
     *
     * @return an EndpointGroup to plug into Javalin
     */
    public static EndpointGroup getDishRoutes() {
        return () -> {
            LOGGER.info("Registering Dish API routes");

            path("/dishes", () -> {

                // --- Retrieval & Filtering ---
                get("/", CONTROLLER::getFilteredDishes, Role.ANYONE); // ?status=&allergen=
                get("/{id}", CONTROLLER::getDishById, Role.ANYONE); // fetch by ID
                get("/available", CONTROLLER::getAvailableDishes, Role.ANYONE); // current availability
                get("/popular", CONTROLLER::getMostOrderedDishes, Role.ANYONE); // most ordered

                // --- Creation ---
                post("/", CONTROLLER::createNewDish, Role.KITCHEN_STAFF, Role.HEAD_CHEF); // simple creation
                post("/full", CONTROLLER::createDishWithRecipeAndIngredients,
                        Role.KITCHEN_STAFF, Role.HEAD_CHEF); // nested with recipe + ingredients

                // --- Partial Updates (PATCH) ---
                patch("/{id}/availableFrom", CONTROLLER::updateDishAvailableFrom, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/availableUntil", CONTROLLER::updateDishAvailableUntil, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/carbohydrates", CONTROLLER::updateDishCarbohydrates, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/description", CONTROLLER::updateDishDescription, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/fat", CONTROLLER::updateDishFat, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/kcal", CONTROLLER::updateDishKcal, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/name", CONTROLLER::updateDishName, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/protein", CONTROLLER::updateDishProtein, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                patch("/{id}/status", CONTROLLER::updateDishStatus, Role.KITCHEN_STAFF, Role.HEAD_CHEF);

                // --- Full-field Updates (PUT) ---
                put("/{id}/allergens", CONTROLLER::updateDishAllergens, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                put("/{id}/availability", CONTROLLER::updateDishAvailability, Role.KITCHEN_STAFF, Role.HEAD_CHEF);
                put("/{id}/recipe", CONTROLLER::updateDishRecipeAndAllergens, Role.KITCHEN_STAFF, Role.HEAD_CHEF);

                // --- Deletion ---
                delete("/{id}", CONTROLLER::deleteExistingDish, Role.HEAD_CHEF, Role.ADMIN);
            });

            LOGGER.info("Dish API routes registered successfully");
        };
    }
}
