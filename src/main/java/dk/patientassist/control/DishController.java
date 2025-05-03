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
 * Controller responsible for handling HTTP requests related to Dish resources.
 * Supports full CRUD operations and validation of enum inputs.
 */
public class DishController {

    private final DishDAO dishDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);

    public DishController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dishDao = DishDAO.getInstance(emf);
    }

    /**
     * Returns a list of all available or sold-out dishes.
     *
     * @param ctx the HTTP context
     */
    public void getAllAvailableDishes(Context ctx) {
        try {
            List<DishDTO> dishes = dishDao.getAllAvailableDishes();
            ctx.status(200).json(dishes);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch available dishes", e);
            throw new NotFoundResponse("Could not retrieve dishes. Please try again later.");
        }
    }

    /**
     * Returns all available dishes (for internal testing).
     *
     * @return list of DishDTOs
     */
    public List<DishDTO> getAllAvailableDishes() {
        return dishDao.getAllAvailableDishes();
    }

    /**
     * Retrieves a dish by its ID.
     *
     * @param ctx the HTTP context with path param "id"
     */
    public void getDishById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO dishDTO = dishDao.getDish(id);
            if (dishDTO == null) {
                LOGGER.warn("Dish with ID {} not found", id);
                throw new NotFoundResponse("Dish with ID " + id + " not found");
            }
            ctx.status(200).json(dishDTO);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid dish ID format", e);
            throw new NotFoundResponse("Invalid dish ID format");
        } catch (Exception e) {
            LOGGER.error("Error retrieving dish by ID", e);
            throw new NotFoundResponse("Could not retrieve dish");
        }
    }

    /**
     * Creates a new dish from the JSON request body.
     * Validates enum fields before persisting.
     *
     * @param ctx the HTTP context
     */
    public void createNewDish(Context ctx) {
        try {
            DishDTO dishDTO = ctx.bodyAsClass(DishDTO.class);

            // Validate enum inputs to catch invalid strings early
            try {
                if (dishDTO.getStatus() != null) {
                    Enum.valueOf(DishStatus.class, dishDTO.getStatus().name());
                }
                if (dishDTO.getAllergens() != null) {
                    Enum.valueOf(Allergens.class, dishDTO.getAllergens().name());
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid enum value in dish creation: {}", e.getMessage());
                throw new BadRequestResponse("Invalid enum value: " + e.getMessage());
            }

            DishDTO created = dishDao.createDish(dishDTO);
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Failed to create new dish", e);
            throw new RuntimeException("Failed to create dish. Please check your input.");
        }
    }

    /**
     * Updates an existing dish by ID.
     * Validates enum fields before updating.
     *
     * @param ctx the HTTP context with path param "id"
     */
    public void updateExistingDish(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO updatedDTO = ctx.bodyAsClass(DishDTO.class);

            // Validate enum inputs to catch invalid strings early
            try {
                if (updatedDTO.getStatus() != null) {
                    Enum.valueOf(DishStatus.class, updatedDTO.getStatus().name());
                }
                if (updatedDTO.getAllergens() != null) {
                    Enum.valueOf(Allergens.class, updatedDTO.getAllergens().name());
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid enum value in dish update: {}", e.getMessage());
                throw new BadRequestResponse("Invalid enum value: " + e.getMessage());
            }

            DishDTO updated = dishDao.updateDish(id, updatedDTO);
            ctx.status(200).json(updated);
        } catch (Exception e) {
            LOGGER.error("Failed to update dish with ID {}", ctx.pathParam("id"), e);
            throw new RuntimeException("Could not update dish. Please verify the ID and data.");
        }
    }

    /**
     * Deletes a dish by ID.
     *
     * @param ctx the HTTP context with path param "id"
     */
    public void deleteExistingDish(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO deleted = dishDao.deleteDish(id);
            ctx.status(200).json(deleted);
        } catch (Exception e) {
            LOGGER.error("Failed to delete dish with ID {}", ctx.pathParam("id"), e);
            throw new RuntimeException("Could not delete dish.");
        }
    }
}