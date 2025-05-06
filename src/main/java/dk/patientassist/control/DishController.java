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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for handling HTTP requests related to Dish entities.
 */
public class DishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);
    private final DishDAO dishDAO;

    public DishController() {
        this.dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    public DishController(DishDAO dishDAO) {
        this.dishDAO = dishDAO;
    }

    public void getAllDishes(Context ctx) {
        ctx.json(dishDAO.getAll());
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
        int id = Integer.parseInt(ctx.pathParam("id"));
        try {
            List<String> allergenStrings = ctx.bodyAsClass(List.class);
            Set<Allergens> allergens = allergenStrings.stream()
                    .map(Allergens::fromString)
                    .collect(Collectors.toSet());

            Optional<DishDTO> updated = dishDAO.updateDishField(id, "allergens", allergens);

            if (updated.isPresent()) {
                ctx.json(updated.get());
            } else {
                ctx.status(404).result("Dish not found");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update allergens on dish {}", id, e);
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    public void updateDishAvailableFrom(Context ctx) {
        handlePatch(ctx, "available_from");
    }

    public void updateDishAvailableUntil(Context ctx) {
        handlePatch(ctx, "available_until");
    }

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
        } catch (IllegalArgumentException | DateTimeParseException e) {
            LOGGER.warn("Patch failed on field={} for ID={} : {}", field, id, e.getMessage());
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }

    private Object parsePatchValue(String field, String value) {
        try {
            return switch (field) {
                case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(value);
                case "status" -> DishStatus.fromString(value);
                case "allergens" -> Allergens.fromString(value);
                case "available_from", "available_until" -> LocalDate.parse(value);
                default -> value;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a new dish including its recipe and ingredients.
     *
     * @param ctx the HTTP context containing the full {@link DishDTO}
     */
    public void createDishWithRecipeAndIngredients(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx)) return;

        try {
            DishDTO created = dishDAO.createWithRecipeAndIngredients(dto);
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Failed to create dish with recipe and ingredients", e);
            ctx.status(500).result("Server error while creating dish with recipe and ingredients");
        }
    }

    /**
     * Updates allergens and recipe on an existing dish.
     *
     * @param ctx the HTTP context containing updated {@link DishDTO}
     */
    public void updateDishRecipeAndAllergens(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (!isDishValid(dto, ctx)) return;

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
     * Validates a full {@link DishDTO} including recipe and ingredients.
     * Responds with HTTP 400 and message if invalid.
     *
     * @param dto the dish to validate
     * @param ctx the HTTP context to return errors to
     * @return true if valid, false if not
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
     * Returns the top N most ordered dishes.
     * Example: /api/dishes/most-ordered?limit=5
     *
     * @param ctx the HTTP context
     */
    public void getMostOrderedDishes(Context ctx) {
        int limit = Optional.ofNullable(ctx.queryParam("limit"))
                .map(Integer::parseInt)
                .orElse(5);

        ctx.json(dishDAO.getMostOrderedDishes(limit));
    }

}
