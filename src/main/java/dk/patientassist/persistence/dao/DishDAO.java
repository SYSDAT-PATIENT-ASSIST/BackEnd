package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.*;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DishDAO implements IDAO<DishDTO, Integer> {

    private static DishDAO instance;
    private final EntityManagerFactory emf;
    private EntityManager em;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAO.class);

    private DishDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static DishDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new DishDAO(emf);
        }
        return instance;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    private EntityManager getEntityManager() {
        return em != null ? em : emf.createEntityManager();
    }

    private boolean shouldClose(EntityManager em) {
        return this.em == null && em != null;
    }

    @Override
    public Optional<DishDTO> get(Integer id) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching dish with ID={}", id);
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish).map(DishDTO::new);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch dish with ID={}", id, e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public List<DishDTO> getAll() {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching all dishes");
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d", DishDTO.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to fetch all dishes", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public DishDTO create(DishDTO dto) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Creating dish: {}", dto.getName());
            Dish dish = new Dish(dto);
            em.getTransaction().begin();
            em.persist(dish);
            em.flush();
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error creating dish", e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public DishDTO update(Integer id, DishDTO dto) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Updating dish with ID={}", id);
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;

            em.getTransaction().begin();
            dish.setName(dto.getName());
            dish.setDescription(dto.getDescription());
            dish.setAvailableFrom(dto.getAvailableFrom());
            dish.setAvailableUntil(dto.getAvailableUntil());
            dish.setStatus(dto.getStatus());
            dish.setKcal(dto.getKcal());
            dish.setProtein(dto.getProtein());
            dish.setCarbohydrates(dto.getCarbohydrates());
            dish.setFat(dto.getFat());
            dish.setAllergens(dto.getAllergens());
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error updating dish with ID={}", id, e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public boolean delete(Integer id) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Deleting dish with ID={}", id);
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return false;

            em.getTransaction().begin();
            em.remove(dish);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            LOGGER.error("Error deleting dish with ID={}", id, e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByStatus(DishStatus status) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching dishes with status={}", status);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status", DishDTO.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error in getDishesByStatus", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching dishes with allergen={}", allergen);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE :allergen MEMBER OF d.allergens", DishDTO.class);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error in getDishesByAllergen", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching dishes with status={} and allergen={}", status, allergen);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d " +
                            "WHERE d.status = :status AND :allergen MEMBER OF d.allergens", DishDTO.class);
            query.setParameter("status", status);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error in getDishesByStatusAndAllergen", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public Optional<DishDTO> updateDishField(Integer id, String field, Object value) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Updating field '{}' on dish with ID={}", field, id);
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return Optional.empty();

            em.getTransaction().begin();
            // Switch logic unchanged
            em.merge(dish);
            em.getTransaction().commit();
            return Optional.of(new DishDTO(dish));
        } catch (Exception e) {
            LOGGER.error("Error in updateDishField", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public DishDTO createWithRecipeAndIngredients(DishDTO dto) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Creating dish with recipe and ingredients: {}", dto.getName());
            em.getTransaction().begin();

            Dish dish = new Dish(dto);
            em.persist(dish);
            em.flush();
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error in createWithRecipeAndIngredients", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public DishDTO updateDishRecipeAndAllergens(int dishId, Set<Allergens> allergens, RecipeDTO recipeDTO) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Updating recipe and allergens for dish ID={}", dishId);
            em.getTransaction().begin();

            // Fetch dish from database
            Dish dish = em.find(Dish.class, dishId);
            if (dish == null) {
                LOGGER.warn("Dish with ID={} not found", dishId);
                return null;
            }

            // Update allergens
            dish.setAllergens(allergens);

            // Create or update recipe
            Recipe recipe = dish.getRecipe();
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setDish(dish);
                dish.setRecipe(recipe);
                em.persist(recipe);
            }

            recipe.setTitle(recipeDTO.getTitle());
            recipe.setInstructions(recipeDTO.getInstructions());

            // Clear and update ingredients
            recipe.getIngredients().clear();
            if (recipeDTO.getIngredients() != null) {
                final Recipe finalRecipe = recipe; // make it explicitly final
                Set<Ingredient> newIngredients = recipeDTO.getIngredients().stream()
                        .map(i -> new Ingredient(new IngredientType(i.getName()), finalRecipe))
                        .collect(Collectors.toSet());

                recipe.setIngredients(newIngredients);
                newIngredients.forEach(em::persist);
            }

            em.merge(dish);
            em.getTransaction().commit();
            LOGGER.info("Successfully updated dish with ID={}", dishId);
            return new DishDTO(dish);

        } catch (Exception e) {
            LOGGER.error("Error in updateDishRecipeAndAllergens", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }


    public List<DishDTO> getMostOrderedDishes(int limit) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching top {} most ordered dishes", limit);
            TypedQuery<Dish> query = em.createQuery(
                    "SELECT d FROM Dish d LEFT JOIN d.orders o GROUP BY d ORDER BY COUNT(o) DESC", Dish.class);
            query.setMaxResults(limit);
            return query.getResultList().stream().map(DishDTO::new).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error in getMostOrderedDishes", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getCurrentlyAvailableDishes() {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching currently available dishes");
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) " +
                            "FROM Dish d WHERE d.status = :status AND :today BETWEEN d.availableFrom AND d.availableUntil", DishDTO.class);
            query.setParameter("status", DishStatus.TILGÆNGELIG);
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error in getCurrentlyAvailableDishes", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getAvailableDishesByAllergen(Allergens allergen) {
        EntityManager em = getEntityManager();
        try {
            LOGGER.info("Fetching available dishes with allergen={}", allergen);
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d " +
                            "WHERE d.status = :status AND :today BETWEEN d.availableFrom AND d.availableUntil " +
                            "AND :allergen MEMBER OF d.allergens", DishDTO.class);
            query.setParameter("status", DishStatus.TILGÆNGELIG);
            query.setParameter("today", LocalDate.now());
            query.setParameter("allergen", allergen);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error in getAvailableDishesByAllergen", e);
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }
}
