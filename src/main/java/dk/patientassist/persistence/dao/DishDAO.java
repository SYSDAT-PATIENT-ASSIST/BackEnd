package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.ent.Dish;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import dk.patientassist.persistence.enums.DishStatus;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DishDAO {

    private static DishDAO instance;
    private static EntityManagerFactory emf;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAO.class);

    public DishDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    public static DishDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new DishDAO(emf);
        }
        return instance;
    }

    public DishDTO createDish(DishDTO dishDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Creating new dish: {}", dishDTO.getName());

            em.getTransaction().begin();
            Dish dish = new Dish(dishDTO);

            dish.setKcal(dishDTO.getKcal());
            dish.setProtein(dishDTO.getProtein());
            dish.setCarbohydrates(dishDTO.getCarbohydrates());
            dish.setFat(dishDTO.getFat());
            dish.setAllergens(dishDTO.getAllergens());

            em.persist(dish);
            em.flush(); // ensure ID is generated
            em.getTransaction().commit();

            LOGGER.info("Dish created with ID: {}", dish.getId());
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to create dish", e);
            throw e;
        }
    }

    public DishDTO getDish(Integer dishId) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Fetching dish with ID: {}", dishId);
            Dish dish = em.find(Dish.class, dishId);
            if (dish == null) {
                LOGGER.warn("Dish with ID {} not found", dishId);
                return null;
            }
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch dish with ID {}", dishId, e);
            throw e;
        }
    }

    public List<DishDTO> getAllAvailableDishes() {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Fetching all available or sold-out dishes");
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) " +
                            "FROM dk.patientassist.persistence.ent.Dish d " +
                            "WHERE d.status = dk.patientassist.persistence.enums.DishStatus.AVAILABLE " +
                            "OR d.status = dk.patientassist.persistence.enums.DishStatus.SOLD_OUT",
                    DishDTO.class
            );
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to fetch available dishes", e);
            throw e;
        }
    }

    public DishDTO updateDish(Integer id, DishDTO updatedDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating dish with ID: {}", id);
            em.getTransaction().begin();

            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Cannot update dish; ID {} not found", id);
                return null;
            }

            dish.setName(updatedDTO.getName());
            dish.setDescription(updatedDTO.getDescription());
            dish.setAvailable_from(updatedDTO.getAvailable_from());
            dish.setAvailable_until(updatedDTO.getAvailable_until());
            dish.setStatus(updatedDTO.getStatus());

            dish.setKcal(updatedDTO.getKcal());
            dish.setProtein(updatedDTO.getProtein());
            dish.setCarbohydrates(updatedDTO.getCarbohydrates());
            dish.setFat(updatedDTO.getFat());
            dish.setAllergens(updatedDTO.getAllergens());

            em.merge(dish);
            em.getTransaction().commit();

            LOGGER.info("Dish with ID {} successfully updated", id);
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update dish with ID {}", id, e);
            throw e;
        }
    }

    public DishDTO deleteDish(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Deleting dish with ID: {}", id);
            em.getTransaction().begin();

            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Cannot delete dish; ID {} not found", id);
                return null;
            }

            em.remove(dish);
            em.getTransaction().commit();

            LOGGER.info("Dish with ID {} successfully deleted", id);
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to delete dish with ID {}", id, e);
            throw e;
        }
    }
}
