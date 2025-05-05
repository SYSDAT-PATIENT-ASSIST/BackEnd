package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Controller for managing HTTP endpoints related to Dish operations.
 */
public class DishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);
    private final DishDAO dishDAO;

    // Default constructor
    public DishController() {
        this.dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    // Testable constructor
    public DishController(DishDAO dishDAO) {
        this.dishDAO = dishDAO;
    }

    public void registerRoutes(Javalin app) {
        app.post("/api/dishes", this::createNewDish);
        app.get("/api/dishes/{id}", this::getDishById);
        app.get("/api/dishes/filter", this::getFilteredDishes);
        app.delete("/api/dishes/{id}", this::deleteExistingDish);

        // Patch routes
        app.patch("/api/dishes/{id}/name", this::updateDishName);
        app.patch("/api/dishes/{id}/description", this::updateDishDescription);
        app.patch("/api/dishes/{id}/kcal", this::updateDishKcal);
        app.patch("/api/dishes/{id}/protein", this::updateDishProtein);
        app.patch("/api/dishes/{id}/carbohydrates", this::updateDishCarbohydrates);
        app.patch("/api/dishes/{id}/fat", this::updateDishFat);
        app.patch("/api/dishes/{id}/status", this::updateDishStatus);
        app.patch("/api/dishes/{id}/allergens", this::updateDishAllergens);
        app.patch("/api/dishes/{id}/available_from", this::updateDishAvailableFrom);
        app.patch("/api/dishes/{id}/available_until", this::updateDishAvailableUntil);
    }

    public void createNewDish(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        DishDTO created = dishDAO.create(dto);
        ctx.status(201).json(created);
    }

    public void getDishById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        dishDAO.get(id).ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Not found"));
    }

    public void deleteExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean deleted = dishDAO.delete(id);
        ctx.status(deleted ? 200 : 404);
    }

    public void getFilteredDishes(Context ctx) {
        String statusParam = ctx.queryParam("status");
        String allergenParam = ctx.queryParam("allergen");

        DishStatus status = statusParam != null ? DishStatus.fromString(statusParam) : null;
        Allergens allergen = allergenParam != null ? Allergens.fromString(allergenParam) : null;

        ctx.json(dishDAO.getDishesByStatusAndAllergen(status, allergen));
    }

    // PATCH handlers
    public void updateDishName(Context ctx) {
        handlePatch(ctx, "name");
    }

    public void updateDishDescription(Context ctx) {
        handlePatch(ctx, "description");
    }

    public void updateDishKcal(Context ctx) {
        handlePatch(ctx, "kcal");
    }

    public void updateDishProtein(Context ctx) {
        handlePatch(ctx, "protein");
    }

    public void updateDishCarbohydrates(Context ctx) {
        handlePatch(ctx, "carbohydrates");
    }

    public void updateDishFat(Context ctx) {
        handlePatch(ctx, "fat");
    }

    public void updateDishStatus(Context ctx) {
        handlePatch(ctx, "status");
    }

    public void updateDishAllergens(Context ctx) {
        handlePatch(ctx, "allergens");
    }

    public void updateDishAvailableFrom(Context ctx) {
        handlePatch(ctx, "available_from");
    }

    public void updateDishAvailableUntil(Context ctx) {
        handlePatch(ctx, "available_until");
    }

    // Shared PATCH logic
    private void handlePatch(Context ctx, String field) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String rawValue = ctx.body();

        try {
            Object parsedValue = parsePatchValue(field, rawValue);
            var updated = dishDAO.updateDishField(id, field, parsedValue);

            if (updated.isPresent()) {
                ctx.json(updated.get());
            } else {
                ctx.status(404).result("Dish not found");
            }
        } catch (Exception e) {
            LOGGER.warn("Patch failed on field={} for ID={} : {}", field, id, e.getMessage());
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    // Helper to parse field types
    private Object parsePatchValue(String field, String value) {
        return switch (field) {
            case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(value);
            case "status" -> DishStatus.fromString(value);
            case "allergens" -> Allergens.fromString(value);
            case "available_from", "available_until" -> LocalDate.parse(value);
            default -> value;
        };
    }
}
