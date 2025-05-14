package dk.patientassist.control;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for handling HTTP endpoints related to {@link DishDTO}.
 * Supports full CRUD operations, patch updates, filtering, and business logic
 * including dish availability and popularity.
 */
public class DishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);
    private final DishDAO dishDAO;

    private static final Set<String> PATCHABLE_FIELDS = Set.of(
            "name", "description", "kcal", "protein", "carbohydrates",
            "fat", "status", "allergens", "availableFrom", "availableUntil");

    public DishController() {
        this.dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    public DishController(DishDAO dishDAO) {
        this.dishDAO = dishDAO;
    }

    /**
     * Returns all dishes in the system.
     */
    public void getAllDishes(Context ctx) {
        ctx.json(dishDAO.getAll());
    }

    /**
     * Creates a new dish from the request body.
     */
    public void createNewDish(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        DishDTO created = dishDAO.create(dto);
        ctx.status(201).json(created);
    }

    /**
     * Returns a single dish by ID.
     */
    public void getDishById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        dishDAO.get(id).ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Not found"));
    }

    /**
     * Deletes a dish by ID.
     */
    public void deleteExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean deleted = dishDAO.delete(id);
        ctx.status(deleted ? 200 : 404);
    }

    /**
     * Returns dishes filtered by optional status and allergen.
     */
    public void getFilteredDishes(Context ctx) {
        String statusParam = ctx.queryParam("status");
        String allergenParam = ctx.queryParam("allergen");

        DishStatus status = statusParam != null ? DishStatus.fromString(statusParam) : null;
        Allergens allergen = allergenParam != null ? Allergens.fromString(allergenParam) : null;

        ctx.json(dishDAO.getDishesByStatusAndAllergen(status, allergen));
    }

    /**
     * Handles PATCH updates to a dish by field name.
     */
    private void handlePatch(Context ctx, String field) {
        if (!PATCHABLE_FIELDS.contains(field)) {
            ctx.status(400).result("Unsupported patch field: " + field);
            return;
        }

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
        } catch (IllegalArgumentException | DateTimeParseException e) {
            LOGGER.warn("Patch failed on field={} for ID={} : {}", field, id, e.getMessage());
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Parses string input into the appropriate object type for partial updates.
     */
    private Object parsePatchValue(String field, String value) {
        try {
            return switch (field) {
                case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(value);
                case "status" -> DishStatus.fromString(value);
                case "allergens" -> Allergens.fromString(value);
                case "availableFrom", "availableUntil" -> LocalDate.parse(value);
                default -> value;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Individual field update endpoints
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

    public void updateDishAvailableFrom(Context ctx) {
        handlePatch(ctx, "availableFrom");
    }

    public void updateDishAvailableUntil(Context ctx) {
        handlePatch(ctx, "availableUntil");
    }

    /**
     * Updates the allergens on a dish using a JSON array of strings.
     */
    public void updateDishAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try {
            List<String> allergenStrings = ctx.bodyAsClass(List.class);
            Set<Allergens> allergens = allergenStrings.stream()
                    .map(Allergens::fromString)
                    .collect(Collectors.toSet());

            Optional<DishDTO> updated = dishDAO.updateDishField(id, "allergens", allergens);
            updated.ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Dish not found"));
        } catch (Exception e) {
            LOGGER.error("Failed to update allergens on dish {}", id, e);
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Fully updates the availability window (from/until) on a dish.
     */
    public void updateDishAvailability(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);

        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null) {
            ctx.status(400).result("Both availableFrom and availableUntil are required.");
            return;
        }

        try {
            Optional<DishDTO> updated = dishDAO.updateDishField(id, "availableFrom", dto.getAvailableFrom());
            dishDAO.updateDishField(id, "availableUntil", dto.getAvailableUntil());

            updated.ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Dish not found"));
        } catch (Exception e) {
            ctx.status(500).result("Server error: " + e.getMessage());
        }
    }

    /**
     * Creates a new dish including its nested recipe and ingredients.
     */
    public void createDishWithRecipeAndIngredients(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx))
            return;

        try {
            DishDTO created = dishDAO.createWithRecipeAndIngredients(dto);
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Failed to create dish with recipe and ingredients", e);
            ctx.status(500).result("Server error while creating dish with recipe and ingredients");
        }
    }

    /**
     * Updates the allergens and recipe (including ingredients) of a dish.
     */
    public void updateDishRecipeAndAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx))
            return;

        try {
            DishDTO updated = dishDAO.updateDishRecipeAndAllergens(id, dto.getAllergens(), dto.getRecipe());
            if (updated != null) {
                ctx.json(updated);
            } else {
                ctx.status(404).result("Dish not found");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update allergens and recipe for dish {}", id, e);
            ctx.status(500).result("Error updating dish");
        }
    }

    /**
     * Validates a full dish structure with required fields, recipe, and
     * ingredients.
     */
    private boolean isDishValid(DishDTO dto, Context ctx) {
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

        boolean hasEmptyIngredient = dto.getRecipe().getIngredients().stream()
                .anyMatch(i -> i.getName() == null || i.getName().isBlank());

        if (hasEmptyIngredient) {
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
     * Returns a list of the most popular dishes based on order count.
     */
    public void getMostOrderedDishes(Context ctx) {
        int limit = Optional.ofNullable(ctx.queryParam("limit"))
                .map(Integer::parseInt)
                .orElse(5);

        ctx.json(dishDAO.getMostOrderedDishes(limit));
    }

    // Retrieves dishes that are currently available and sold out for patient menu
    public void getAllAvailable(Context ctx){
        try {
            List<DishDTO> dishDTOS = dishDAO.getAllAvailable();
            ctx.res().setStatus(200);
            ctx.json(dishDTOS, DishDTO.class);
        } catch (Exception e) {
            throw new NotFoundResponse("No content found for this request");
        }
    }

    //used for cucumber / menuStepDefinitions
    public List<DishDTO> getAllAvailable(){
        return dishDAO.getAllAvailable();
    }


    /**
     * Returns dishes that are currently available based on today's date.
     * If an allergen is provided as a query param, filters results to dishes that
     * include that allergen.
     * Example:
     * GET /api/dishes/available
     * GET /api/dishes/available?allergen=SENNEP
     *
     * @param ctx the HTTP context
     */
    public void getAvailableDishes(Context ctx) {
        String allergenParam = ctx.queryParam("allergen");

        if (allergenParam != null) {
            try {
                Allergens allergen = Allergens.fromString(allergenParam);
                ctx.json(dishDAO.getAvailableDishesByAllergen(allergen));
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Invalid allergen: " + allergenParam);
            }
        } else {
            ctx.json(dishDAO.getCurrentlyAvailableDishes());
        }
    }
}
