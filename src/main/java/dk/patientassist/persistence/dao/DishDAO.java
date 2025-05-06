package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Ingredient;
import dk.patientassist.persistence.ent.IngredientType;
import dk.patientassist.persistence.ent.Recipe;
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
 * Data Access Object (DAO) for handling persistence logic related to {@link Dish} entities.
 * Provides operations such as create, read, update, delete (CRUD),
 * as well as filtering, nested recipe handling and popularity ranking.
 */
public class DishDAO implements IDAO<DishDTO, Integer> {

    private static DishDAO instance;
    private final EntityManagerFactory emf;
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

    @Override
    public Optional<DishDTO> get(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish).map(DishDTO::new);
        }
    }

    @Override
    public List<DishDTO> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d", DishDTO.class);
            return query.getResultList();
        }
    }

    @Override
    public DishDTO create(DishDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = new Dish(dto);
            em.getTransaction().begin();
            em.persist(dish);
            em.flush();
            em.getTransaction().commit();
            return new DishDTO(dish);
        }
    }

    @Override
    public DishDTO update(Integer id, DishDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
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
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return false;

            em.getTransaction().begin();
            em.remove(dish);
            em.getTransaction().commit();
            return true;
        }
    }

    public List<DishDTO> getDishesByStatus(DishStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status",
                    DishDTO.class);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        }
    }

    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d " +
                            "WHERE d.status = :status AND :allergen MEMBER OF d.allergens",
                    DishDTO.class);
            query.setParameter("status", status);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        }
    }

    public Optional<DishDTO> updateDishField(Integer id, String field, Object value) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return Optional.empty();

            em.getTransaction().begin();
            switch (field) {
                case "name" -> dish.setName((String) value);
                case "description" -> dish.setDescription((String) value);
                case "kcal" -> dish.setKcal(castDouble(value));
                case "protein" -> dish.setProtein(castDouble(value));
                case "carbohydrates" -> dish.setCarbohydrates(castDouble(value));
                case "fat" -> dish.setFat(castDouble(value));
                case "status" -> dish.setStatus((DishStatus) value);
                case "allergens" -> {
                    if (value instanceof Set<?> set) {
                        @SuppressWarnings("unchecked")
                        Set<Allergens> allergens = (Set<Allergens>) set;
                        dish.setAllergens(allergens);
                    } else {
                        throw new IllegalArgumentException("Expected Set<Allergens>");
                    }
                }
                case "availableFrom" -> dish.setAvailableFrom((LocalDate) value);
                case "availableUntil" -> dish.setAvailableUntil((LocalDate) value);
                default -> throw new IllegalArgumentException("Unsupported field: " + field);
            }

            em.merge(dish);
            em.getTransaction().commit();
            return Optional.of(new DishDTO(dish));
        }
    }

    private double castDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        throw new IllegalArgumentException("Expected numeric value");
    }

    public DishDTO createWithRecipeAndIngredients(DishDTO dto) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Dish dish = new Dish(dto);

            if (dto.getRecipe() != null) {
                RecipeDTO recipeDTO = dto.getRecipe();
                Recipe recipe = new Recipe();
                recipe.setTitle(recipeDTO.getTitle());
                recipe.setInstructions(recipeDTO.getInstructions());

                if (recipeDTO.getIngredients() != null) {
                    Set<Ingredient> ingredients = recipeDTO.getIngredients().stream()
                            .map(i -> new Ingredient(new IngredientType(i.getName()), recipe))
                            .collect(Collectors.toSet());
                    recipe.setIngredients(ingredients);
                }

                recipe.setDish(dish);
                dish.setRecipe(recipe);
            }

            em.persist(dish);
            em.flush();
            em.getTransaction().commit();

            return new DishDTO(dish);
        } catch (Exception e) {
            em.getTransaction().rollback();
            LOGGER.error("Failed to create dish with recipe and ingredients", e);
            throw e;
        } finally {
            em.close();
        }
    }

    public DishDTO updateDishRecipeAndAllergens(int dishId, Set<Allergens> allergens, RecipeDTO recipeDTO) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, dishId);
            if (dish == null) return null;

            dish.setAllergens(allergens);

            Recipe recipe = dish.getRecipe();
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setDish(dish);
                dish.setRecipe(recipe);
                em.persist(recipe);
            }

            recipe.setTitle(recipeDTO.getTitle());
            recipe.setInstructions(recipeDTO.getInstructions());
            recipe.getIngredients().clear();
            em.flush();

            if (recipeDTO.getIngredients() != null) {
                final Recipe finalRecipe = recipe;
                Set<Ingredient> newIngredients = recipeDTO.getIngredients().stream()
                        .map(i -> new Ingredient(new IngredientType(i.getName()), finalRecipe))
                        .collect(Collectors.toSet());
                recipe.setIngredients(newIngredients);
                newIngredients.forEach(em::persist);
            }

            em.merge(dish);
            em.getTransaction().commit();

            return new DishDTO(dish);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.error("Failed to update recipe and allergens for dish ID {}", dishId, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<DishDTO> getMostOrderedDishes(int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Dish> query = em.createQuery(
                    "SELECT d FROM Dish d LEFT JOIN d.orders o GROUP BY d ORDER BY COUNT(o) DESC",
                    Dish.class);
            query.setMaxResults(limit);
            return query.getResultList().stream().map(DishDTO::new).collect(Collectors.toList());
        }
    }
}
