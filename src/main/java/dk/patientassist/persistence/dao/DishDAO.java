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

    /** Thread-safe singleton */
    public static synchronized DishDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new DishDAO(emf);
        }
        return instance;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    private EntityManager getEntityManager() {
        return (em != null) ? em : emf.createEntityManager();
    }

    private boolean shouldClose(EntityManager em) {
        return this.em == null && em != null;
    }

    @Override
    public Optional<DishDTO> get(Integer id) {
        EntityManager em = getEntityManager();
        try {
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish).map(DishDTO::new);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public List<DishDTO> getAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Dish> q = em.createQuery("SELECT d FROM Dish d", Dish.class);
            return q.getResultList().stream()
                    .map(DishDTO::new)
                    .collect(Collectors.toList());
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    @Override
    public DishDTO create(DishDTO dto) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = new Dish(dto);
            em.persist(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
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
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return null;

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
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return false;
            em.remove(dish);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByStatus(DishStatus status) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status",
                    DishDTO.class);
            q.setParameter("status", status);
            return q.getResultList();
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            q.setParameter("allergen", allergen);
            return q.getResultList();
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) "
                            + "FROM Dish d WHERE d.status = :status AND :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            q.setParameter("status", status);
            q.setParameter("allergen", allergen);
            return q.getResultList();
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public Optional<DishDTO> updateDishField(Integer id, String field, Object value) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return Optional.empty();

            switch (field) {
                case "name"            -> dish.setName((String) value);
                case "description"     -> dish.setDescription((String) value);
                case "kcal"            -> dish.setKcal((Double) value);
                case "protein"         -> dish.setProtein((Double) value);
                case "carbohydrates"   -> dish.setCarbohydrates((Double) value);
                case "fat"             -> dish.setFat((Double) value);
                case "status"          -> dish.setStatus((DishStatus) value);
                case "allergens"       -> dish.setAllergens((Set<Allergens>) value);  // only via dedicated endpoint
                case "availableFrom"   -> dish.setAvailableFrom((LocalDate) value);
                case "availableUntil"  -> dish.setAvailableUntil((LocalDate) value);
                default -> throw new IllegalArgumentException("Cannot patch field: " + field);
            }

            em.merge(dish);
            em.getTransaction().commit();
            return Optional.of(new DishDTO(dish));
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /** Single-transaction update of both availability dates */
    public DishDTO updateAvailability(int id, LocalDate from, LocalDate until) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, id);
            if (dish == null) {
                em.getTransaction().rollback();
                return null;
            }
            dish.setAvailableFrom(from);
            dish.setAvailableUntil(until);
            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /** Nested‐create: dish + recipe + ingredients */
    public DishDTO createWithRecipeAndIngredients(DishDTO dto) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            // Dish
            Dish dish = new Dish(dto);
            em.persist(dish);

            // Recipe
            Recipe recipe = new Recipe();
            recipe.setTitle(dto.getRecipe().getTitle());
            recipe.setInstructions(dto.getRecipe().getInstructions());
            recipe.setDish(dish);
            dish.setRecipe(recipe);
            em.persist(recipe);

            // Ingredients
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
            return new DishDTO(dish);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    /** Update both allergens & recipe/ingredients */
    public DishDTO updateDishRecipeAndAllergens(int dishId, Set<Allergens> allergens, RecipeDTO recipeDTO) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            Dish dish = em.find(Dish.class, dishId);
            if (dish == null) {
                em.getTransaction().rollback();
                return null;
            }

            // Allergens
            dish.setAllergens(allergens);

            // Recipe
            Recipe recipe = dish.getRecipe();
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setDish(dish);
                dish.setRecipe(recipe);
                em.persist(recipe);
            }
            recipe.setTitle(recipeDTO.getTitle());
            recipe.setInstructions(recipeDTO.getInstructions());

            // Clear and re‐add ingredients
            recipe.getIngredients().clear();
            if (recipeDTO.getIngredients() != null) {
                for (var ingrDTO : recipeDTO.getIngredients()) {
                    IngredientType type = new IngredientType(ingrDTO.getName());
                    Ingredient ingr = new Ingredient(type, recipe);
                    recipe.getIngredients().add(ingr);
                    em.persist(type);
                    em.persist(ingr);
                }
            }

            em.merge(dish);
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getMostOrderedDishes(int limit) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Dish> q = em.createQuery(
                    "SELECT d FROM Dish d LEFT JOIN d.orders o GROUP BY d ORDER BY COUNT(o) DESC",
                    Dish.class);
            q.setMaxResults(limit);
            return q.getResultList().stream()
                    .map(DishDTO::new)
                    .collect(Collectors.toList());
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getCurrentlyAvailableDishes() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<DishDTO> q = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) "
                            + "FROM Dish d WHERE d.status = :status "
                            + "AND :today BETWEEN d.availableFrom AND d.availableUntil",
                    DishDTO.class);
            q.setParameter("status", DishStatus.TILGÆNGELIG);
            q.setParameter("today", LocalDate.now());
            return q.getResultList();
        } finally {
            if (shouldClose(em)) em.close();
        }
    }

    public List<DishDTO> getAvailableDishesByAllergen(Allergens allergen) {
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
        } finally {
            if (shouldClose(em)) em.close();
        }
    }
}
