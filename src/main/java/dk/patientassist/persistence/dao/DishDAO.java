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

/**
 * DAO for {@link Dish} entities, exposing CRUD operations,
 * various filters, and nested recipe/ingredient handling.
 * <p>
 * Uses JPA EntityManagerFactory for persistence,
 * and ensures thread-safe singleton access.
 */
public class DishDAO implements IDAO<DishDTO, Integer> {

    private static DishDAO instance;
    private final EntityManagerFactory emf;
    private EntityManager em;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAO.class);

    private DishDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Returns the singleton instance of DishDAO.
     *
     * @param emf EntityManagerFactory to use
     * @return shared DishDAO instance
     */
    public static synchronized DishDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            LOGGER.info("Creating new DishDAO singleton");
            instance = new DishDAO(emf);
        }
        return instance;
    }

    /**
     * Override the EntityManager (for testing).
     *
     * @param em EntityManager to use
     */
    public void setEntityManager(EntityManager em) {
        LOGGER.debug("Setting custom EntityManager for DishDAO");
        this.em = em;
    }

    private EntityManager getEntityManager() {
        return (em != null) ? em : emf.createEntityManager();
    }

    private boolean shouldClose(EntityManager em) {
        return this.em == null && em != null;
    }

    /**
     * Find a dish by its ID.
     *
     * @param id primary key
     * @return Optional of DishDTO if found
     */
    @Override
    public Optional<DishDTO> get(Integer id) {
        LOGGER.info("Fetching Dish with id={}", id);
        EntityManager em = getEntityManager();
        try {
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish).map(DishDTO::new);
        } catch (Exception e) {
            LOGGER.error("Error fetching Dish id={}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Retrieve all dishes.
     *
     * @return list of all DishDTO
     */
    @Override
    public List<DishDTO> getAll() {
        LOGGER.info("Retrieving all Dishes");
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Dish> q = em.createQuery("SELECT d FROM Dish d", Dish.class);
            return q.getResultList().stream()
                    .map(DishDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error retrieving all Dishes: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Create a new dish.
     *
     * @param dto data to persist
     * @return the created DishDTO
     */
    @Override
    public DishDTO create(DishDTO dto) {
        LOGGER.info("Creating Dish '{}'", dto.getName());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = new Dish(dto);
            em.persist(dish);
            em.getTransaction().commit();
            LOGGER.debug("Created Dish with id={}", dish.getId());
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error creating Dish '{}': {}", dto.getName(), e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Update all fields of an existing dish.
     *
     * @param id  ID to update
     * @param dto new data
     * @return updated DishDTO or null if not found
     */
    @Override
    public DishDTO update(Integer id, DishDTO dto) {
        LOGGER.info("Updating Dish id={}", id);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Dish id={} not found for update", id);
                return null;
            }

            dish.setName(dto.getName());
            dish.setDescription(dto.getDescription());
            dish.setAvailableFrom(dto.getAvailableFrom());
            dish.setAvailableUntil(dto.getAvailableUntil());
            dish.setStatus(dto.getStatus());
            dish.setKcal(dto.getKcal());
            dish.setProtein(dto.getProtein());
            dish.setCarbohydrates(dto.getCarbohydrates());
            dish.setFat(dto.getFat());
            dish.setAllergens(dto.getAllergens() != null ? new HashSet<>(dto.getAllergens()) : new HashSet<>());

            em.merge(dish);
            em.getTransaction().commit();
            LOGGER.debug("Updated Dish id={}", id);
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error updating Dish id={}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }


    /**
     * Delete a dish by ID.
     *
     * @param id to remove
     * @return true if removed, false if not found
     */
    @Override
    public boolean delete(Integer id) {
        LOGGER.info("Deleting Dish id={}", id);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Dish id={} not found for deletion", id);
                return false;
            }
            em.remove(dish);
            em.getTransaction().commit();
            LOGGER.debug("Deleted Dish id={}", id);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error deleting Dish id={}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Find dishes matching a status.
     *
     * @param status to filter
     * @return list of DishDTO
     */
    public List<DishDTO> getDishesByStatus(DishStatus status) {
        LOGGER.info("Querying Dishes by status={}", status);
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status",
                    DishDTO.class);
            q.setParameter("status", status);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error querying by status={}: {}", status, e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Find dishes containing a given allergen.
     *
     * @param allergen to filter
     * @return list of DishDTO
     */
    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        LOGGER.info("Querying Dishes by allergen={}", allergen);
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            q.setParameter("allergen", allergen);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error querying by allergen={}: {}", allergen, e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Find dishes by both status and allergen.
     *
     * @param status   required status
     * @param allergen required allergen
     * @return filtered list
     */
    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        LOGGER.info("Querying Dishes by status={} and allergen={}", status, allergen);
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) "
                            + "FROM Dish d WHERE d.status = :status AND :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            q.setParameter("status", status);
            q.setParameter("allergen", allergen);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error querying by status={} and allergen={}: {}", status, allergen, e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Patch a single field on a dish.
     *
     * @param id    target ID
     * @param field field name
     * @param value new value
     * @return Optional of updated DishDTO
     */
    public Optional<DishDTO> updateDishField(Integer id, String field, Object value) {
        LOGGER.info("Patching field='{}' on Dish id={}", field, id);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Dish id={} not found for patch", id);
                return Optional.empty();
            }

            switch (field) {
                case "name"            -> dish.setName((String) value);
                case "description"     -> dish.setDescription((String) value);
                case "kcal"            -> dish.setKcal((Double) value);
                case "protein"         -> dish.setProtein((Double) value);
                case "carbohydrates"   -> dish.setCarbohydrates((Double) value);
                case "fat"             -> dish.setFat((Double) value);
                case "status"          -> dish.setStatus((DishStatus) value);
                case "allergens"       -> dish.setAllergens((Set<Allergens>) value);
                case "availableFrom"   -> dish.setAvailableFrom((LocalDate) value);
                case "availableUntil"  -> dish.setAvailableUntil((LocalDate) value);
                default -> throw new IllegalArgumentException("Cannot patch field: " + field);
            }

            em.merge(dish);
            em.getTransaction().commit();
            LOGGER.debug("Patched field='{}' on Dish id={}", field, id);
            return Optional.of(new DishDTO(dish));
        } catch (Exception e) {
            LOGGER.error("Error patching Dish id={} field='{}': {}", id, field, e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Atomically update availability window.
     *
     * @param id    dish ID
     * @param from  new start date
     * @param until new end date
     * @return updated DishDTO or null if not found
     */
    public DishDTO updateAvailability(int id, LocalDate from, LocalDate until) {
        LOGGER.info("Updating availability for Dish id={} from={} to={}", id, from, until);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                LOGGER.warn("Dish id={} not found for availability update", id);
                em.getTransaction().rollback();
                return null;
            }
            dish.setAvailableFrom(from);
            dish.setAvailableUntil(until);
            em.merge(dish);
            em.getTransaction().commit();
            LOGGER.debug("Updated availability for Dish id={}", id);
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error updating availability for Dish id={}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Create a dish along with its recipe and ingredients in one transaction.
     *
     * @param dto containing dish + recipe + ingredients
     * @return created DishDTO
     */
    public DishDTO createWithRecipeAndIngredients(DishDTO dto) {
        LOGGER.info("Creating Dish with recipe and ingredients '{}'", dto.getName());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            Dish dish = new Dish(dto);
            em.persist(dish);

            Recipe recipe = new Recipe();
            recipe.setTitle(dto.getRecipe().getTitle());
            recipe.setInstructions(dto.getRecipe().getInstructions());
            recipe.setDish(dish);
            dish.setRecipe(recipe);
            em.persist(recipe);

            for (var ingrDTO : dto.getRecipe().getIngredients()) {
                String name = ingrDTO.getName();
                IngredientType type = em.createQuery(
                                "SELECT t FROM IngredientType t WHERE t.name = :n", IngredientType.class)
                        .setParameter("n", name)
                        .getResultStream()
                        .findFirst()
                        .orElseGet(() -> {
                            IngredientType t = new IngredientType(name);
                            em.persist(t);
                            return t;
                        });
                Ingredient ingr = new Ingredient(type, recipe);
                recipe.getIngredients().add(ingr);
                em.persist(ingr);
            }

            em.getTransaction().commit();
            LOGGER.debug("Created Dish with nested recipe id={}", dish.getId());
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Error creating nested Dish '{}': {}", dto.getName(), e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Update both allergens and recipe/ingredients in one transaction.
     *
     * @param dishId    target dish ID
     * @param allergens new allergen set
     * @param recipeDTO new recipe data
     * @return updated DishDTO or null if not found
     */
    public DishDTO updateDishRecipeAndAllergens(int dishId, Set<Allergens> allergens, RecipeDTO recipeDTO) {
        LOGGER.info("Updating recipe & allergens for Dish id={}", dishId);
        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            Dish dish = em.find(Dish.class, dishId);
            if (dish == null) {
                LOGGER.warn("Dish id={} not found for recipe/allergen update", dishId);
                em.getTransaction().rollback();
                return null;
            }

            // Safely copy allergens to avoid immutable collection issues
            dish.setAllergens(allergens != null ? new HashSet<>(allergens) : new HashSet<>());

            Recipe recipe = dish.getRecipe();
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setDish(dish);
                dish.setRecipe(recipe);
                em.persist(recipe);
            }

            recipe.setTitle(recipeDTO.getTitle());
            recipe.setInstructions(recipeDTO.getInstructions());

            // Clear and rebuild ingredients
            recipe.getIngredients().clear();

            if (recipeDTO.getIngredients() != null) {
                for (var ingrDTO : recipeDTO.getIngredients()) {
                    String name = ingrDTO.getName();
                    IngredientType type = em.createQuery(
                                    "SELECT t FROM IngredientType t WHERE t.name = :name", IngredientType.class)
                            .setParameter("name", name)
                            .getResultStream()
                            .findFirst()
                            .orElseGet(() -> {
                                IngredientType newType = new IngredientType(name);
                                em.persist(newType);
                                return newType;
                            });

                    Ingredient ingredient = new Ingredient(type, recipe);
                    recipe.getIngredients().add(ingredient);
                    em.persist(ingredient);
                }
            }

            em.merge(dish);
            em.getTransaction().commit();
            LOGGER.debug("Updated nested recipe & allergens for Dish id={}", dishId);
            return new DishDTO(dish);

        } catch (Exception e) {
            LOGGER.error("Error updating nested data for Dish id={}: {}", dishId, e.getMessage(), e);
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }


    /**
     * Return the top-N most ordered dishes.
     *
     * @param limit max number of results
     * @return list of DishDTO ordered by popularity
     */
    public List<DishDTO> getMostOrderedDishes(int limit) {
        LOGGER.info("Fetching top {} most ordered Dishes", limit);
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Dish> q = em.createQuery(
                    "SELECT d FROM Dish d LEFT JOIN d.orders o GROUP BY d ORDER BY COUNT(o) DESC",
                    Dish.class);
            q.setMaxResults(limit);
            return q.getResultList().stream()
                    .map(DishDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error fetching most ordered Dishes: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /**
     * Get dishes currently available.
     *
     * @return list of available DishDTO
     */
    public List<DishDTO> getCurrentlyAvailableDishes() {
        LOGGER.info("Querying currently available Dishes");
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Dish> q = em.createQuery(
                    "SELECT d FROM Dish d WHERE d.status = :status AND :today BETWEEN d.availableFrom AND d.availableUntil",
                    Dish.class);
            q.setParameter("status", DishStatus.TILGÆNGELIG);
            q.setParameter("today", LocalDate.now());

            return q.getResultList().stream()
                    .map(DishDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error querying available Dishes: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }


    /**
     * Get currently available dishes filtered by allergen.
     *
     * @param allergen allergen to exclude
     * @return filtered list of DishDTO
     */
    public List<DishDTO> getAvailableDishesByAllergen(Allergens allergen) {
        LOGGER.info("Querying available Dishes excluding allergen={}", allergen);
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) "
                            + "FROM Dish d WHERE d.status = :status "
                            + "AND :today BETWEEN d.availableFrom AND d.availableUntil "
                            + "AND :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            q.setParameter("status", DishStatus.TILGÆNGELIG);
            q.setParameter("today", LocalDate.now());
            q.setParameter("allergen", allergen);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error("Error querying available Dishes by allergen={}: {}", allergen, e.getMessage(), e);
            throw e;
        } finally {
            if (shouldClose(em)) em.close();
        }
    }
}
