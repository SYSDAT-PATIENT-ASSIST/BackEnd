package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DishController {

    private final DishDAO dishDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishController.class);

    public DishController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dishDao = DishDAO.getInstance(emf);
    }

    public void getAllAvailableDishes(Context ctx) {
        try {
            List<DishDTO> dishes = dishDao.getAllAvailableDishes();
            ctx.status(200).json(dishes);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch available dishes", e);
            throw new NotFoundResponse("Could not retrieve dishes. Please try again later.");
        }
    }

    public List<DishDTO> getAllAvailableDishes() {
        return dishDao.getAllAvailableDishes();
    }

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

    public void createNewDish(Context ctx) {
        try {
            DishDTO dishDTO = ctx.bodyAsClass(DishDTO.class);
            DishDTO created = dishDao.createDish(dishDTO);
            ctx.status(201).json(created);
        } catch (Exception e) {
            LOGGER.error("Failed to create new dish", e);
            throw new RuntimeException("Failed to create dish. Please check your input.");
        }
    }

    public void updateExistingDish(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DishDTO updatedDTO = ctx.bodyAsClass(DishDTO.class);
            DishDTO updated = dishDao.updateDish(id, updatedDTO);
            ctx.status(200).json(updated);
        } catch (Exception e) {
            LOGGER.error("Failed to update dish with ID {}", ctx.pathParam("id"), e);
            throw new RuntimeException("Could not update dish. Please verify the ID and data.");
        }
    }

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
