package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

public class DishController {

    private final DishDAO dishDao;

    public DishController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dishDao = DishDAO.getInstance(emf);
    }

    public void getAllAvailableDishes(Context ctx) {
        try {
            List<DishDTO> dishes = dishDao.getAllAvailableDishes();
            ctx.status(200).json(dishes);
        } catch (Exception e) {
            throw new NotFoundResponse("No content found for this request");
        }
    }

    // Used in Cucumber tests
    public List<DishDTO> getAllAvailableDishes() {
        return dishDao.getAllAvailableDishes();
    }

    public void getDishById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO dishDTO = dishDao.getDish(id);
        if (dishDTO == null) {
            throw new NotFoundResponse("Dish with id " + id + " not found");
        }
        ctx.status(200).json(dishDTO);
    }

    public void createNewDish(Context ctx) {
        DishDTO dishDTO = ctx.bodyAsClass(DishDTO.class);
        DishDTO created = dishDao.createDish(dishDTO);
        ctx.status(201).json(created);
    }

    public void updateExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO updatedDTO = ctx.bodyAsClass(DishDTO.class);
        DishDTO updated = dishDao.updateDish(id, updatedDTO);
        ctx.status(200).json(updated);
    }

    public void deleteExistingDish(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        DishDTO deleted = dishDao.deleteDish(id);
        ctx.status(200).json(deleted);
    }
}
