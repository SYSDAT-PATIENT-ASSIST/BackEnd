package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for {@link DishDTO} endpoints.
 * <p>
 * Handles CRUD operations, partial updates, filtering,
 * and business logic for dish availability and popularity.
 */
public class DishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);
    private final DishDAO dishDAO;

    private static final Set<String> PATCHABLE_FIELDS = Set.of(
            "name", "description", "kcal", "protein", "carbohydrates",
            "fat", "status", "availableFrom", "availableUntil"
    );

    /**
     * Default constructor using configured EntityManagerFactory.
     */
    public DishController() {
        this(DishDAO.getInstance(HibernateConfig.getEntityManagerFactory()));
    }

    /**
     * Constructor for dependency injection/testing.
     *
     * @param dishDAO DAO instance to use
     */
    public DishController(DishDAO dishDAO) {
        LOGGER.debug("DishController instantiated with custom DAO");
        this.dishDAO = dishDAO;
    }

    /**
     * GET /dishes
     * Returns all dishes.
     *
     * @param ctx HTTP context
     */
    public void getAllDishes(Context ctx) {
        LOGGER.info("GET /dishes - retrieving all dishes");
        List<DishDTO> dishes = dishDAO.getAll();
        LOGGER.debug("Found {} dishes", dishes.size());
        ctx.json(dishes);
    }

    /**
     * POST /dishes
     * Creates a new dish from JSON body.
     *
     * @param ctx HTTP context
     */
    public void createNewDish(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        LOGGER.info("POST /dishes - create new dish '{}'", dto.getName());
        try {
            DishDTO created = dishDAO.create(dto);
            LOGGER.debug("Created dish with id={}", created.getId());
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Error creating dish '{}': {}", dto.getName(), e.getMessage(), e);
            ctx.status(500).result("Failed to create dish");
        }
    }

    /**
     * GET /dishes/:id
     * Returns a dish by ID.
     *
     * @param ctx HTTP context
     */
    public void getDishById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("GET /dishes/{} - fetching dish by id", id);
        try {
            Optional<DishDTO> opt = dishDAO.get(id);
            if (opt.isPresent()) {
                LOGGER.debug("Dish id={} found", id);
                ctx.json(opt.get());
            } else {
                LOGGER.warn("Dish id={} not found", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500).result("Failed to fetch dish");
        }
    }

    /**
     * DELETE /dishes/:id
     * Deletes a dish by ID.
     *
     * @param ctx HTTP context
     */
    public void deleteExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("DELETE /dishes/{} - deleting dish", id);
        try {
            boolean deleted = dishDAO.delete(id);
            if (deleted) {
                LOGGER.debug("Dish id={} deleted", id);
                ctx.status(200).result("Deleted");
            } else {
                LOGGER.warn("Dish id={} not found for deletion", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500).result("Failed to delete dish");
        }
    }

    /**
     * GET /dishes/filter
     * Filters dishes by status and/or allergen.
     *
     * @param ctx HTTP context
     */
    public void getFilteredDishes(Context ctx) {
        String statusParam   = ctx.queryParam("status");
        String allergenParam = ctx.queryParam("allergen");
        LOGGER.info("GET /dishes/filter - status='{}', allergen='{}'", statusParam, allergenParam);

        DishStatus status   = null;
        Allergens  allergen = null;
        try {
            if (statusParam   != null) status   = DishStatus.fromString(statusParam);
            if (allergenParam != null) allergen = Allergens.fromString(allergenParam);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid filter parameters: status='{}', allergen='{}'", statusParam, allergenParam);
            ctx.status(400).result("Invalid status or allergen");
            return;
        }

        List<DishDTO> results;
        if      (status   != null && allergen != null) results = dishDAO.getDishesByStatusAndAllergen(status, allergen);
        else if (status   != null)                     results = dishDAO.getDishesByStatus(status);
        else if (allergen != null)                     results = dishDAO.getDishesByAllergen(allergen);
        else                                           results = dishDAO.getAll();

        LOGGER.debug("Filtered dishes count: {}", results.size());
        ctx.json(results);
    }

    /**
     * PATCH helper for individual fields.
     *
     * @param ctx   HTTP context
     * @param field field name to patch
     */
    private void handlePatch(Context ctx, String field) {
        LOGGER.info("PATCH /dishes/:id/{} - patching field", field);
        if (!PATCHABLE_FIELDS.contains(field)) {
            LOGGER.warn("Unsupported patch field: '{}'", field);
            ctx.status(400).result("Unsupported patch field: " + field);
            return;
        }
        int id = Integer.parseInt(ctx.pathParam("id"));
        String raw = ctx.body();
        try {
            Object val = parsePatchValue(field, raw);
            Optional<DishDTO> upd = dishDAO.updateDishField(id, field, val);
            if (upd.isPresent()) {
                LOGGER.debug("Patched field '{}' for dish id={}", field, id);
                ctx.json(upd.get());
            } else {
                LOGGER.warn("Dish id={} not found for patch", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error patching field '{}' for id={}: {}", field, id, e.getMessage(), e);
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Parses raw string to appropriate type for patching.
     *
     * @param field field name
     * @param val   raw string body
     * @return parsed value
     */
    private Object parsePatchValue(String field, String val) {
        return switch (field) {
            case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(val);
            case "status"                                -> DishStatus.fromString(val);
            case "availableFrom", "availableUntil"      -> LocalDate.parse(val);
            default                                       -> val;
        };
    }

    /** Delegates PATCH /dishes/:id/name */
    public void updateDishName(Context ctx)            { handlePatch(ctx, "name"); }
    /** Delegates PATCH /dishes/:id/description */
    public void updateDishDescription(Context ctx)     { handlePatch(ctx, "description"); }
    /** Delegates PATCH /dishes/:id/kcal */
    public void updateDishKcal(Context ctx)            { handlePatch(ctx, "kcal"); }
    /** Delegates PATCH /dishes/:id/protein */
    public void updateDishProtein(Context ctx)         { handlePatch(ctx, "protein"); }
    /** Delegates PATCH /dishes/:id/carbohydrates */
    public void updateDishCarbohydrates(Context ctx)   { handlePatch(ctx, "carbohydrates"); }
    /** Delegates PATCH /dishes/:id/fat */
    public void updateDishFat(Context ctx)             { handlePatch(ctx, "fat"); }
    /** Delegates PATCH /dishes/:id/status */
    public void updateDishStatus(Context ctx)          { handlePatch(ctx, "status"); }
    /** Delegates PATCH /dishes/:id/availableFrom */
    public void updateDishAvailableFrom(Context ctx)   { handlePatch(ctx, "availableFrom"); }
    /** Delegates PATCH /dishes/:id/availableUntil */
    public void updateDishAvailableUntil(Context ctx)  { handlePatch(ctx, "availableUntil"); }

    /**
     * PUT /dishes/:id/allergens
     * Updates allergens on a dish.
     *
     * @param ctx HTTP context
     */
    public void updateDishAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("PUT /dishes/{}/allergens - updating allergens", id);
        List<String> list = ctx.bodyAsClass(List.class);
        Set<Allergens> set = list.stream()
                .map(Allergens::fromString)
                .collect(Collectors.toSet());
        try {
            Optional<DishDTO> upd = dishDAO.updateDishField(id, "allergens", set);
            if (upd.isPresent()) {
                LOGGER.debug("Updated allergens for dish id={}", id);
                ctx.json(upd.get());
            } else {
                LOGGER.warn("Dish id={} not found for allergens update", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating allergens for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(400).result("Invalid allergens");
        }
    }

    /**
     * PUT /dishes/:id/availability
     * Updates availability window in one transaction.
     *
     * @param ctx HTTP context
     */
    public void updateDishAvailability(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("PUT /dishes/{}/availability - updating availability", id);
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null) {
            LOGGER.warn("Availability update missing dates for dish id={}", id);
            ctx.status(400).result("Both availableFrom and availableUntil are required.");
            return;
        }
        try {
            DishDTO updated = dishDAO.updateAvailability(id, dto.getAvailableFrom(), dto.getAvailableUntil());
            if (updated != null) {
                LOGGER.debug("Availability updated for dish id={}", id);
                ctx.json(updated);
            } else {
                LOGGER.warn("Dish id={} not found for availability update", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating availability for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500).result("Failed to update availability");
        }
    }

    /**
     * POST /dishes/full
     * Creates a dish with recipe and ingredients.
     *
     * @param ctx HTTP context
     */
    public void createDishWithRecipeAndIngredients(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        LOGGER.info("POST /dishes/full - creating dish with recipe '{}'", dto.getName());
        if (!isDishValid(dto, ctx)) return;
        try {
            DishDTO created = dishDAO.createWithRecipeAndIngredients(dto);
            LOGGER.debug("Created full dish with id={}", created.getId());
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Error creating full dish '{}': {}", dto.getName(), e.getMessage(), e);
            ctx.status(500).result("Failed to create dish with recipe");
        }
    }

    /**
     * PUT /dishes/:id/recipe
     * Updates allergens and recipe (with ingredients) for a dish.
     *
     * @param ctx HTTP context
     */
    public void updateDishRecipeAndAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("PUT /dishes/{}/recipe - updating recipe & allergens", id);
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx)) return;
        try {
            DishDTO updated = dishDAO.updateDishRecipeAndAllergens(id, dto.getAllergens(), dto.getRecipe());
            if (updated != null) {
                LOGGER.debug("Updated recipe & allergens for dish id={}", id);
                ctx.json(updated);
            } else {
                LOGGER.warn("Dish id={} not found for recipe update", id);
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating recipe & allergens for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500).result("Failed to update recipe");
        }
    }

    /**
     * Validates the DishDTO before full create/update.
     *
     * @param dto dish payload
     * @param ctx HTTP context (for error responses)
     * @return true if valid, false otherwise
     */
    private boolean isDishValid(DishDTO dto, Context ctx) {
        LOGGER.debug("Validating DishDTO before full create/update");
        if (dto.getAllergens() == null || dto.getAllergens().isEmpty()) {
            ctx.status(400).result("Allergens must not be empty.");
            return false;
        }
        if (dto.getRecipe() == null) {
            ctx.status(400).result("Recipe must be provided.");
            return false;
        }
        if (dto.getRecipe().getTitle() == null || dto.getRecipe().getTitle().isBlank()) {
            ctx.status(400).result("Recipe title is required.");
            return false;
        }
        if (dto.getRecipe().getInstructions() == null || dto.getRecipe().getInstructions().isBlank()) {
            ctx.status(400).result("Recipe instructions are required.");
            return false;
        }
        if (dto.getRecipe().getIngredients() == null || dto.getRecipe().getIngredients().isEmpty()) {
            ctx.status(400).result("At least one ingredient is required.");
            return false;
        }
        boolean emptyName = dto.getRecipe().getIngredients().stream()
                .anyMatch(i -> i.getName() == null || i.getName().isBlank());
        if (emptyName) {
            ctx.status(400).result("All ingredients must have a name.");
            return false;
        }
        if (dto.getKcal() < 0 || dto.getProtein() < 0 || dto.getFat() < 0 || dto.getCarbohydrates() < 0) {
            ctx.status(400).result("Nutritional values must be non-negative.");
            return false;
        }
        return true;
    }

    /**
     * GET /dishes/popular
     * Returns the most ordered dishes, with optional limit queryParam.
     *
     * @param ctx HTTP context
     */
    public void getMostOrderedDishes(Context ctx) {
        String limitParam = ctx.queryParam("limit");
        int limit = Optional.ofNullable(limitParam)
                .map(Integer::parseInt)
                .orElse(5);
        LOGGER.info("GET /dishes/popular - limit={}", limit);
        try {
            List<DishDTO> result = dishDAO.getMostOrderedDishes(limit);
            LOGGER.debug("Returning {} popular dishes", result.size());
            ctx.json(result);
        } catch (Exception e) {
            LOGGER.error("Error fetching popular dishes: {}", e.getMessage(), e);
            ctx.status(500).result("Failed to fetch popular dishes");
        }
    }

    /**
     * GET /dishes/available
     * Returns currently available dishes, optional allergen filter.
     *
     * @param ctx HTTP context
     */
    public void getAvailableDishes(Context ctx) {
        String param = ctx.queryParam("allergen");
        LOGGER.info("GET /dishes/available - allergen filter='{}'", param);
        try {
            if (param != null) {
                Allergens a = Allergens.fromString(param);
                List<DishDTO> result = dishDAO.getAvailableDishesByAllergen(a);
                LOGGER.debug("Returning {} available dishes with allergen={}", result.size(), a);
                ctx.json(result);
            } else {
                List<DishDTO> result = dishDAO.getCurrentlyAvailableDishes();
                LOGGER.debug("Returning {} currently available dishes", result.size());
                ctx.json(result);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid allergen filter='{}'", param);
            ctx.status(400).result("Invalid allergen");
        } catch (Exception e) {
            LOGGER.error("Error fetching available dishes: {}", e.getMessage(), e);
            ctx.status(500).result("Failed to fetch available dishes");
        }
    }
}
