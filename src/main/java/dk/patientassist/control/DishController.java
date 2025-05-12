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
        this.dishDAO = dishDAO;
    }

    /**
     * GET /dishes
     * Returns all dishes.
     *
     * @param ctx HTTP context
     */
    public void getAllDishes(Context ctx) {
        LOGGER.info("getAllDishes called");
        List<DishDTO> dishes = dishDAO.getAll();
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
        LOGGER.info("createNewDish called with name={}", dto.getName());
        DishDTO created = dishDAO.create(dto);
        ctx.status(201).json(created);
    }

    /**
     * GET /dishes/:id
     * Returns a dish by ID.
     *
     * @param ctx HTTP context
     */
    public void getDishById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("getDishById called with id={}", id);
        Optional<DishDTO> opt = dishDAO.get(id);
        if (opt.isPresent()) {
            ctx.json(opt.get());
        } else {
            ctx.status(404).result("Not found");
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
        LOGGER.info("deleteExistingDish called with id={}", id);
        boolean deleted = dishDAO.delete(id);
        ctx.status(deleted ? 200 : 404);
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
        LOGGER.info("getFilteredDishes called with status={} allergen={}", statusParam, allergenParam);

        DishStatus status   = null;
        Allergens  allergen = null;
        try {
            if (statusParam   != null) status   = DishStatus.fromString(statusParam);
            if (allergenParam != null) allergen = Allergens.fromString(allergenParam);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid filter parameter: {} or {}", statusParam, allergenParam);
            ctx.status(400).result("Invalid status or allergen");
            return;
        }

        List<DishDTO> results;
        if      (status   != null && allergen != null) results = dishDAO.getDishesByStatusAndAllergen(status, allergen);
        else if (status   != null)                     results = dishDAO.getDishesByStatus(status);
        else if (allergen != null)                     results = dishDAO.getDishesByAllergen(allergen);
        else                                           results = dishDAO.getAll();

        ctx.json(results);
    }

    /**
     * PATCH helper for individual fields.
     *
     * @param ctx   HTTP context
     * @param field field name to patch
     */
    private void handlePatch(Context ctx, String field) {
        LOGGER.info("handlePatch called for field={}", field);
        if (!PATCHABLE_FIELDS.contains(field)) {
            LOGGER.warn("Unsupported patch field: {}", field);
            ctx.status(400).result("Unsupported patch field: " + field);
            return;
        }
        int id = Integer.parseInt(ctx.pathParam("id"));
        String raw = ctx.body();
        try {
            Object val = parsePatchValue(field, raw);
            Optional<DishDTO> upd = dishDAO.updateDishField(id, field, val);
            if (upd.isPresent()) {
                ctx.json(upd.get());
            } else {
                ctx.status(404).result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error patching field {} for id={}: {}", field, id, e.getMessage());
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Parses raw string to appropriate type for patching.
     */
    private Object parsePatchValue(String field, String val) {
        return switch (field) {
            case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(val);
            case "status"                                -> DishStatus.fromString(val);
            case "availableFrom", "availableUntil"      -> LocalDate.parse(val);
            default                                       -> val;
        };
    }

    public void updateDishName(Context ctx)            { handlePatch(ctx, "name"); }
    public void updateDishDescription(Context ctx)     { handlePatch(ctx, "description"); }
    public void updateDishKcal(Context ctx)            { handlePatch(ctx, "kcal"); }
    public void updateDishProtein(Context ctx)         { handlePatch(ctx, "protein"); }
    public void updateDishCarbohydrates(Context ctx)   { handlePatch(ctx, "carbohydrates"); }
    public void updateDishFat(Context ctx)             { handlePatch(ctx, "fat"); }
    public void updateDishStatus(Context ctx)          { handlePatch(ctx, "status"); }
    public void updateDishAvailableFrom(Context ctx)   { handlePatch(ctx, "availableFrom"); }
    public void updateDishAvailableUntil(Context ctx)  { handlePatch(ctx, "availableUntil"); }

    /**
     * PUT /dishes/:id/allergens
     * Updates allergens on a dish.
     *
     * @param ctx HTTP context
     */
    public void updateDishAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("updateDishAllergens called for id={}", id);
        List<String> list = ctx.bodyAsClass(List.class);
        Set<Allergens> set = list.stream()
                .map(Allergens::fromString)
                .collect(Collectors.toSet());
        Optional<DishDTO> upd = dishDAO.updateDishField(id, "allergens", set);
        if (upd.isPresent()) {
            ctx.json(upd.get());
        } else {
            ctx.status(404).result("Not found");
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
        LOGGER.info("updateDishAvailability called for id={}", id);
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null) {
            ctx.status(400).result("Both availableFrom and availableUntil are required.");
            return;
        }
        DishDTO updated = dishDAO.updateAvailability(id, dto.getAvailableFrom(), dto.getAvailableUntil());
        if (updated != null) {
            ctx.json(updated);
        } else {
            ctx.status(404).result("Not found");
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
        LOGGER.info("createDishWithRecipeAndIngredients called with name={}", dto.getName());
        if (!isDishValid(dto, ctx)) return;
        DishDTO created = dishDAO.createWithRecipeAndIngredients(dto);
        ctx.status(201).json(created);
    }

    /**
     * PUT /dishes/:id/recipe
     * Updates allergens and recipe (with ingredients) for a dish.
     *
     * @param ctx HTTP context
     */
    public void updateDishRecipeAndAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("updateDishRecipeAndAllergens called for id={}", id);
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx)) return;
        DishDTO updated = dishDAO.updateDishRecipeAndAllergens(id, dto.getAllergens(), dto.getRecipe());
        if (updated != null) {
            ctx.json(updated);
        } else {
            ctx.status(404).result("Not found");
        }
    }

    /**
     * Validates the DishDTO before full create/update.
     */
    private boolean isDishValid(DishDTO dto, Context ctx) {
        if (dto.getAllergens() == null || dto.getAllergens().isEmpty()) {
            ctx.status(400).result("Allergens must not be empty."); return false;
        }
        if (dto.getRecipe() == null) {
            ctx.status(400).result("Recipe must be provided."); return false;
        }
        if (dto.getRecipe().getTitle() == null || dto.getRecipe().getTitle().isBlank()) {
            ctx.status(400).result("Recipe title is required."); return false;
        }
        if (dto.getRecipe().getInstructions() == null || dto.getRecipe().getInstructions().isBlank()) {
            ctx.status(400).result("Recipe instructions are required."); return false;
        }
        if (dto.getRecipe().getIngredients() == null || dto.getRecipe().getIngredients().isEmpty()) {
            ctx.status(400).result("At least one ingredient is required."); return false;
        }
        boolean empty = dto.getRecipe().getIngredients().stream()
                .anyMatch(i -> i.getName() == null || i.getName().isBlank());
        if (empty) {
            ctx.status(400).result("All ingredients must have a name."); return false;
        }
        if (dto.getKcal() < 0 || dto.getProtein() < 0 || dto.getFat() < 0 || dto.getCarbohydrates() < 0) {
            ctx.status(400).result("Nutritional values must be non-negative."); return false;
        }
        return true;
    }

    /**
     * GET /dishes/popular
     * Returns the most ordered dishes, with optional limit queryParam.
     */
    public void getMostOrderedDishes(Context ctx) {
        String limitParam = ctx.queryParam("limit");
        int limit = Optional.ofNullable(limitParam)
                .map(Integer::parseInt)
                .orElse(5);
        LOGGER.info("getMostOrderedDishes called with limit={}", limit);
        ctx.json(dishDAO.getMostOrderedDishes(limit));
    }

    /**
     * GET /dishes/available
     * Returns currently available dishes, optional allergen filter.
     */
    public void getAvailableDishes(Context ctx) {
        String param = ctx.queryParam("allergen");
        LOGGER.info("getAvailableDishes called with allergen={}", param);
        if (param != null) {
            try {
                Allergens a = Allergens.fromString(param);
                ctx.json(dishDAO.getAvailableDishesByAllergen(a));
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Invalid allergen");
            }
        } else {
            ctx.json(dishDAO.getCurrentlyAvailableDishes());
        }
    }
}