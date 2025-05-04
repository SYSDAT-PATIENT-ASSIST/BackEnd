package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO class for managing {@link Dish} entities.
 * Provides CRUD operations and query methods for filtering and updating dish data.
 */
public class DishDAO {

    private static DishDAO instance;
    private final EntityManagerFactory emf;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAO.class);

    DishDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static DishDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new DishDAO(emf);
        }
        return instance;
    }

    public DishDTO createDish(DishDTO dishDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Creating new dish: {}", dishDTO.getName());
            em.getTransaction().begin();
            Dish dish = new Dish(dishDTO);
            em.persist(dish);
            em.flush();
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
            return dish != null ? new DishDTO(dish) : null;
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
                            "FROM Dish d " +
                            "WHERE d.status = dk.patientassist.persistence.enums.DishStatus.TILGÆNGELIG " +
                            "OR d.status = dk.patientassist.persistence.enums.DishStatus.UDSOLGT", DishDTO.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to fetch available dishes", e);
            throw e;
        }
    }

    public List<DishDTO> getDishesByStatus(DishStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Filtering dishes by status: {}", status);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status",
                    DishDTO.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to filter dishes by status", e);
            throw e;
        }
    }

    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Filtering dishes by allergen: {}", allergen);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.allergens = :allergen",
                    DishDTO.class);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to filter dishes by allergen", e);
            throw e;
        }
    }

    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        try {
            if (status != null && allergen == null) {
                return getDishesByStatus(status);
            } else if (status == null && allergen != null) {
                return getDishesByAllergen(allergen);
            } else if (status != null && allergen != null) {
                try (EntityManager em = emf.createEntityManager()) {
                    TypedQuery<DishDTO> query = em.createQuery(
                            "SELECT new dk.patientassist.persistence.dto.DishDTO(d) " +
                                    "FROM Dish d WHERE d.status = :status AND d.allergens = :allergen", DishDTO.class);
                    query.setParameter("status", status);
                    query.setParameter("allergen", allergen);
                    return query.getResultList();
                }
            } else {
                return getAllAvailableDishes();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to filter dishes by status and/or allergen", e);
            throw e;
        }
    }

    public DishDTO findDishByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Finding dish by name: {}", name);
            TypedQuery<Dish> query = em.createQuery(
                    "SELECT d FROM Dish d WHERE d.name = :name", Dish.class);
            query.setParameter("name", name);
            Dish dish = query.getResultStream().findFirst().orElse(null);
            return dish != null ? new DishDTO(dish) : null;
        } catch (Exception e) {
            LOGGER.error("Failed to find dish by name: {}", name, e);
            throw e;
        }
    }

    public DishDTO updateDish(Integer id, DishDTO updatedDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating full dish with ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Dish with ID {} not found", id);
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
                LOGGER.warn("Dish with ID {} not found", id);
                return null;
            }
            em.remove(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to delete dish with ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishStatus(Integer id, DishStatus newStatus) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating status of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setStatus(newStatus);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update status for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishAllergens(Integer id, Allergens newAllergen) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating allergens of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setAllergens(newAllergen);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update allergens for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishName(Integer id, String newName) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating name of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setName(newName);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update name for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishDescription(Integer id, String newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating description of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setDescription(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update description for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishKcal(Integer id, double newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating kcal of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setKcal(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update kcal for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishProtein(Integer id, double newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating protein of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setProtein(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update protein for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishCarbohydrates(Integer id, double newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating carbohydrates of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setCarbohydrates(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update carbohydrates for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishFat(Integer id, double newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating fat of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setFat(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update fat for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishAvailableFrom(Integer id, LocalDate newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating available_from of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setAvailable_from(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update available_from for dish ID {}", id, e);
            throw e;
        }
    }

    public DishDTO updateDishAvailableUntil(Integer id, LocalDate newValue) {
        try (EntityManager em = emf.createEntityManager()) {
            LOGGER.info("Updating available_until of dish ID: {}", id);
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;
            dish.setAvailable_until(newValue);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to update available_until for dish ID {}", id, e);
            throw e;
        }
    }

}
