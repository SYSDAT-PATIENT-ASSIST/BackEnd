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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsible for managing Dish resources.
 * Supports full CRUD operations including validation and filtering.
 */
public class DishController {

    private final DishDAO dishDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);

    public DishController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dishDao = DishDAO.getInstance(emf);
    }

    /**
     * Returns a list of all dishes that are AVAILABLE or SOLD_OUT.
     * @param ctx HTTP context
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
     * Internal access method for test usage.
     * @return all available dishes
     */
    public List<DishDTO> getAllAvailableDishes() {
        return dishDao.getAllAvailableDishes();
    }

    /**
     * Retrieves a dish by its ID.
     * @param ctx Context with pathParam("id")
     */
    public void getDishById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO dish = dishDao.getDish(id);
            if (dish == null) {
                throw new NotFoundResponse("Dish with ID " + id + " not found");
            }
            ctx.status(200).json(dish);
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid dish ID format.");
        }
    }

    /**
     * Creates a new dish and returns it.
     * @param ctx JSON body with dish details
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
     * Updates a dish with a given ID.
     * @param ctx Path param "id" and JSON body
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
     * @param ctx Context with dish ID pathParam
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
     * Filters dishes by optional query parameters: status and allergen.
     * @param ctx HTTP query parameters "status" and "allergen"
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
}
