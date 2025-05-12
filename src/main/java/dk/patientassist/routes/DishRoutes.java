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
     * <p>
     * - GET    /api/dishes                    → list & filter<br>
     * - GET    /api/dishes/{id}               → fetch by id<br>
     * - GET    /api/dishes/available          → currently available (opt. allergen filter)<br>
     * - GET    /api/dishes/popular            → most ordered (opt. limit)<br>
     * - POST   /api/dishes                    → create new dish<br>
     * - POST   /api/dishes/full               → create with recipe & ingredients<br>
     * - PATCH  /api/dishes/{id}/{field}       → partial updates (name, kcal, status, etc.)<br>
     * - PUT    /api/dishes/{id}/availability  → update availability window<br>
     * - PUT    /api/dishes/{id}/allergens     → replace allergen set<br>
     * - PUT    /api/dishes/{id}/recipe        → update recipe & allergens<br>
     * - DELETE /api/dishes/{id}               → delete dish<br>
     *
     * @return an EndpointGroup to plug into Javalin
     */
    public static EndpointGroup getDishRoutes() {
        return () -> {
            LOGGER.info("Registering Dish API routes");
            path("/api/dishes", () -> {

                // --- Retrieval & Filtering ---
                get("/", CONTROLLER::getFilteredDishes, Role.ANYONE);                   // list / filter by ?status=&allergen=
                get("/{id}", CONTROLLER::getDishById, Role.ANYONE);                    // fetch by id
                get("/available", CONTROLLER::getAvailableDishes, Role.ANYONE);        // available dishes
                get("/popular", CONTROLLER::getMostOrderedDishes, Role.ANYONE);        // popular dishes

                // --- Creation ---
                post("/", CONTROLLER::createNewDish, Role.KØKKENPERSONALE, Role.HOVEDKOK);       // simple create
                post("/full", CONTROLLER::createDishWithRecipeAndIngredients,
                        Role.KØKKENPERSONALE, Role.HOVEDKOK);                                   // nested create

                // --- Partial Updates (PATCH) ---
                patch("/{id}/name",        CONTROLLER::updateDishName,            Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/description", CONTROLLER::updateDishDescription,     Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/kcal",        CONTROLLER::updateDishKcal,            Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/protein",     CONTROLLER::updateDishProtein,         Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/carbohydrates",CONTROLLER::updateDishCarbohydrates,   Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/fat",         CONTROLLER::updateDishFat,             Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/status",      CONTROLLER::updateDishStatus,          Role.KØKKENPERSONALE, Role.HOVEDKOK);
                patch("/{id}/availableFrom",   CONTROLLER::updateDishAvailableFrom,   Role.HOVEDKOK);
                patch("/{id}/availableUntil",  CONTROLLER::updateDishAvailableUntil,  Role.HOVEDKOK);

                // --- Full-field Updates (PUT) ---
                put("/{id}/availability", CONTROLLER::updateDishAvailability, Role.HOVEDKOK);
                put("/{id}/allergens",    CONTROLLER::updateDishAllergens,    Role.KØKKENPERSONALE, Role.HOVEDKOK);
                put("/{id}/recipe",       CONTROLLER::updateDishRecipeAndAllergens,
                        Role.KØKKENPERSONALE, Role.HOVEDKOK);

                // --- Deletion ---
                delete("/{id}", CONTROLLER::deleteExistingDish, Role.HOVEDKOK, Role.ADMIN);

            });
            LOGGER.info("Dish API routes registered successfully");
        };
    }
}
