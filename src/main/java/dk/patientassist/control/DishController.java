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

public class DishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);
    private final DishDAO dishDAO;


    private static final Set<String> PATCHABLE_FIELDS = Set.of(
            "name", "description", "kcal", "protein", "carbohydrates",
            "fat", "status", "availableFrom", "availableUntil"
    );

    public DishController(DishDAO dao) {
        this.dishDAO = dao;
    }

    public DishController() {
        this(DishDAO.getInstance(HibernateConfig.getEntityManagerFactory()));
    }

    public void getAllDishes(Context ctx) {
        try {
            List<DishDTO> dishes = dishDAO.getAll();
            dishes.forEach(d -> System.out.println("Dish: " + d.getName()));
            ctx.result("Fetched " + dishes.size() + " dishes");

        } catch (Exception e) {
            LOGGER.error("Error in getAllDishes: {}", e.getMessage(), e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void createNewDish(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        LOGGER.info("POST /dishes - create new dish '{}'", dto.getName());
        try {
            DishDTO created = dishDAO.create(dto);
            LOGGER.debug("Created dish with id={}", created.getId());
            ctx.status(201);
            ctx.json(created);
        } catch (Exception e) {
            LOGGER.error("Error creating dish '{}': {}", dto.getName(), e.getMessage(), e);
            ctx.status(500);
            ctx.result("Failed to create dish");
        }
    }

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
            LOGGER.error("Error fetching dish id={}: {}", id, e.getMessage(), e); // <-- print full stack
            ctx.status(500).result("Failed to fetch dish");
        }
    }


    public void deleteExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("DELETE /dishes/{} - deleting dish", id);
        try {
            boolean deleted = dishDAO.delete(id);
            if (deleted) {
                LOGGER.debug("Dish id={} deleted", id);
                ctx.status(200);
                ctx.result("Deleted");
            } else {
                LOGGER.warn("Dish id={} not found for deletion", id);
                ctx.status(404);
                ctx.result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500).result("Failed to delete dish: " + e.getMessage());
        }
    }

    public void getFilteredDishes(Context ctx) {
        String statusParam = ctx.queryParam("status");
        String allergenParam = ctx.queryParam("allergen");
        LOGGER.info("GET /dishes/filter - status='{}', allergen='{}'", statusParam, allergenParam);

        DishStatus status = null;
        Allergens allergen = null;

        try {
            if (statusParam != null) status = DishStatus.fromString(statusParam);
            if (allergenParam != null) allergen = Allergens.fromString(allergenParam);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid filter parameters: status='{}', allergen='{}'", statusParam, allergenParam);
            ctx.status(400).result("Invalid status or allergen");
            return;
        }

        try {
            List<DishDTO> results;
            if (status != null && allergen != null) results = dishDAO.getDishesByStatusAndAllergen(status, allergen);
            else if (status != null) results = dishDAO.getDishesByStatus(status);
            else if (allergen != null) results = dishDAO.getDishesByAllergen(allergen);
            else results = dishDAO.getAll();

            LOGGER.debug("Filtered dishes count: {}", results.size());
            ctx.json(results);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION in getFilteredDishes", e); // prints full stack trace
            ctx.status(500).result("Server error: " + e.getMessage());
        }
    }

    private void handlePatch(Context ctx, String field) {
        LOGGER.info("PATCH /dishes/:id/{} - patching field", field);
        if (!PATCHABLE_FIELDS.contains(field)) {
            LOGGER.warn("Unsupported patch field: '{}'", field);
            ctx.status(400);
            ctx.result("Unsupported patch field: " + field);
            return;
        }
        int id = Integer.parseInt(ctx.pathParam("id"));
        String raw = ctx.bodyAsClass(String.class);

        try {
            Object val = parsePatchValue(field, raw);
            Optional<DishDTO> upd = dishDAO.updateDishField(id, field, val);
            if (upd.isPresent()) {
                LOGGER.debug("Patched field '{}' for dish id={}", field, id);
                ctx.json(upd.get());
            } else {
                LOGGER.warn("Dish id={} not found for patch", id);
                ctx.status(404);
                ctx.result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error patching field '{}' for id={}: {}", field, id, e.getMessage(), e);
            ctx.status(400);
            ctx.result("Invalid input: " + e.getMessage());
        }
    }

    private Object parsePatchValue(String field, String val) {
        return switch (field) {
            case "kcal", "protein", "carbohydrates", "fat" -> Double.parseDouble(val);
            case "status" -> DishStatus.fromString(val);
            case "availableFrom", "availableUntil" -> LocalDate.parse(val);
            default -> val;
        };
    }

    public void updateDishName(Context ctx) { handlePatch(ctx, "name"); }
    public void updateDishDescription(Context ctx) { handlePatch(ctx, "description"); }
    public void updateDishKcal(Context ctx) { handlePatch(ctx, "kcal"); }
    public void updateDishProtein(Context ctx) { handlePatch(ctx, "protein"); }
    public void updateDishCarbohydrates(Context ctx) { handlePatch(ctx, "carbohydrates"); }
    public void updateDishFat(Context ctx) { handlePatch(ctx, "fat"); }
    public void updateDishStatus(Context ctx) { handlePatch(ctx, "status"); }
    public void updateDishAvailableFrom(Context ctx) { handlePatch(ctx, "availableFrom"); }
    public void updateDishAvailableUntil(Context ctx) { handlePatch(ctx, "availableUntil"); }

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
                ctx.status(404);
                ctx.result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating allergens for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(400);
            ctx.result("Invalid allergens");
        }
    }

    public void updateDishAvailability(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LOGGER.info("PUT /dishes/{}/availability - updating availability", id);
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null) {
            LOGGER.warn("Availability update missing dates for dish id={}", id);
            ctx.status(400);
            ctx.result("Both availableFrom and availableUntil are required.");
            return;
        }
        try {
            DishDTO updated = dishDAO.updateAvailability(id, dto.getAvailableFrom(), dto.getAvailableUntil());
            if (updated != null) {
                LOGGER.debug("Availability updated for dish id={}", id);
                ctx.json(updated);
            } else {
                LOGGER.warn("Dish id={} not found for availability update", id);
                ctx.status(404);
                ctx.result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating availability for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500);
            ctx.result("Failed to update availability");
        }
    }

    public void createDishWithRecipeAndIngredients(Context ctx) {
        DishDTO dto = ctx.bodyAsClass(DishDTO.class);
        LOGGER.info("POST /dishes/full - creating dish with recipe '{}'", dto.getName());
        if (!isDishValid(dto, ctx)) return;
        try {
            DishDTO created = dishDAO.createWithRecipeAndIngredients(dto);
            LOGGER.debug("Created full dish with id={}", created.getId());
            ctx.status(201);
            ctx.json(created);
        } catch (Exception e) {
            LOGGER.error("Error creating full dish '{}': {}", dto.getName(), e.getMessage(), e);
            ctx.status(500);
            ctx.result("Failed to create dish with recipe");
        }
    }

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
                ctx.status(404);
                ctx.result("Not found");
            }
        } catch (Exception e) {
            LOGGER.error("Error updating recipe & allergens for dish id={}: {}", id, e.getMessage(), e);
            ctx.status(500);
            ctx.result("Failed to update recipe");
        }
    }

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

    public void getMostOrderedDishes(Context ctx) {
        String limitParam = ctx.queryParam("limit");
        int limit = Optional.ofNullable(limitParam).map(Integer::parseInt).orElse(5);
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
