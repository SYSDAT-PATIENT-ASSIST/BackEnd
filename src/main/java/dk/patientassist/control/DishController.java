package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.BadRequestResponse;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsible for managing Dish resources.
 * Supports full CRUD operations, filtering, and partial updates.
 */
public class DishController {

    private final DishDAO dishDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);

    public DishController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dishDao = DishDAO.getInstance(emf);
    }

    /**
     * Returns all available or sold-out dishes.
     */
    public void getAllAvailableDishes(Context ctx) {
        try {
            List<DishDTO> dishes = dishDao.getAllAvailableDishes();
            ctx.status(200).json(dishes);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch available dishes", e);
            throw new NotFoundResponse("Could not retrieve dishes.");
        }
    }

    /**
     * Internal helper to fetch available dishes for test/code use.
     */
    public List<DishDTO> getAllAvailableDishes() {
        return dishDao.getAllAvailableDishes();
    }

    /**
     * Retrieves a dish by ID.
     */
    public void getDishById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO dish = dishDao.getDish(id);
            if (dish == null) throw new NotFoundResponse("Dish with ID " + id + " not found");
            ctx.status(200).json(dish);
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid dish ID format.");
        }
    }

    /**
     * Creates a new dish.
     */
    public void createNewDish(Context ctx) {
        try {
            DishDTO dto = ctx.bodyAsClass(DishDTO.class);
            if (dto.getStatus() != null) DishStatus.valueOf(dto.getStatus().name());
            if (dto.getAllergens() != null) Allergens.valueOf(dto.getAllergens().name());

            DishDTO created = dishDao.createDish(dto);
            ctx.status(201).json(created).result("Dish created successfully.");
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Invalid enum: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error creating dish", e);
            throw new BadRequestResponse("Failed to create dish.");
        }
    }

    /**
     * Updates an existing dish by ID.
     */
    public void updateExistingDish(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO dto = ctx.bodyAsClass(DishDTO.class);
            if (dto.getStatus() != null) DishStatus.valueOf(dto.getStatus().name());
            if (dto.getAllergens() != null) Allergens.valueOf(dto.getAllergens().name());

            DishDTO updated = dishDao.updateDish(id, dto);
            ctx.status(200).json(updated).result("Dish updated successfully.");
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Invalid enum: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error updating dish", e);
            throw new BadRequestResponse("Failed to update dish.");
        }
    }

    /**
     * Deletes a dish by ID.
     */
    public void deleteExistingDish(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO deleted = dishDao.deleteDish(id);
            if (deleted == null) throw new NotFoundResponse("Dish not found.");
            ctx.status(200).json(deleted).result("Dish deleted successfully.");
        } catch (Exception e) {
            LOGGER.error("Error deleting dish", e);
            throw new BadRequestResponse("Could not delete dish.");
        }
    }

    /**
     * Returns a filtered list of dishes based on status and allergen.
     */
    public void getFilteredDishes(Context ctx) {
        try {
            String statusParam = ctx.queryParam("status");
            String allergenParam = ctx.queryParam("allergen");

            DishStatus status = statusParam != null ? DishStatus.fromString(statusParam) : null;
            Allergens allergen = allergenParam != null ? Allergens.fromString(allergenParam) : null;

            List<DishDTO> result = dishDao.getDishesByStatusAndAllergen(status, allergen);
            ctx.status(200).json(result);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid filter input: {}", e.getMessage());
            ctx.status(400).result("Invalid filter value.");
        }
    }

    /**
     * PATCH endpoint to update the dish's status.
     */
    public void updateDishStatus(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishStatus newStatus = DishStatus.fromString(ctx.body());
            DishDTO updated = dishDao.updateDishStatus(id, newStatus);
            if (updated == null) throw new NotFoundResponse("Dish not found");
            ctx.status(200).json(updated).result("Dish status updated.");
        } catch (Exception e) {
            LOGGER.error("Failed to update dish status", e);
            throw new BadRequestResponse("Invalid status or failed to update.");
        }
    }

    /**
     * PATCH endpoint to update the dish's allergen.
     */
    public void updateDishAllergens(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Allergens newAllergen = Allergens.fromString(ctx.body());
            DishDTO updated = dishDao.updateDishAllergens(id, newAllergen);
            if (updated == null) throw new NotFoundResponse("Dish not found");
            ctx.status(200).json(updated).result("Dish allergen updated.");
        } catch (Exception e) {
            LOGGER.error("Failed to update dish allergen", e);
            throw new BadRequestResponse("Invalid allergen or failed to update.");
        }
    }

    /**
     * PATCH endpoint to update the dish's name.
     */
    public void updateDishName(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String newName = ctx.body();
            if (newName == null || newName.isBlank()) throw new BadRequestResponse("Name cannot be blank.");
            DishDTO updated = dishDao.updateDishName(id, newName);
            if (updated == null) throw new NotFoundResponse("Dish not found.");
            ctx.status(200).json(updated).result("Dish name updated.");
        } catch (Exception e) {
            LOGGER.error("Failed to update dish name", e);
            throw new BadRequestResponse("Failed to update name.");
        }
    }

    /** PATCH: update dish description */
    public void updateDishDescription(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String description = ctx.body();
        if (description == null || description.isBlank()) throw new BadRequestResponse("Description cannot be blank.");
        DishDTO updated = dishDao.updateDishDescription(id, description);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Description updated.");
    }

    /** PATCH: update kcal */
    public void updateDishKcal(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        double kcal = Double.parseDouble(ctx.body());
        DishDTO updated = dishDao.updateDishKcal(id, kcal);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Kcal updated.");
    }

    /** PATCH: update protein */
    public void updateDishProtein(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        double protein = Double.parseDouble(ctx.body());
        DishDTO updated = dishDao.updateDishProtein(id, protein);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Protein updated.");
    }

    /** PATCH: update carbohydrates */
    public void updateDishCarbohydrates(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        double carbs = Double.parseDouble(ctx.body());
        DishDTO updated = dishDao.updateDishCarbohydrates(id, carbs);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Carbohydrates updated.");
    }

    /** PATCH: update fat */
    public void updateDishFat(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        double fat = Double.parseDouble(ctx.body());
        DishDTO updated = dishDao.updateDishFat(id, fat);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Fat updated.");
    }

    /** PATCH: update availableFrom */
    public void updateDishAvailableFrom(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LocalDate from = LocalDate.parse(ctx.body());
        DishDTO updated = dishDao.updateDishAvailableFrom(id, from);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Available from updated.");
    }

    /** PATCH: update availableUntil */
    public void updateDishAvailableUntil(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        LocalDate until = LocalDate.parse(ctx.body());
        DishDTO updated = dishDao.updateDishAvailableUntil(id, until);
        if (updated == null) throw new NotFoundResponse("Dish not found.");
        ctx.status(200).json(updated).result("Available until updated.");
    }
}
