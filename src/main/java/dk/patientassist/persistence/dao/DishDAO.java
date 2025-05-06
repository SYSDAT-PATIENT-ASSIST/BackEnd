package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Ingredient;
import dk.patientassist.persistence.ent.Recipe;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO implementation for managing {@link Dish} entities using JPA.
 * Provides CRUD operations, filtered queries, and partial updates via {@link DishDTO}.
 */
public class DishDAO implements IDAO<DishDTO, Integer> {

    private static DishDAO instance;
    private final EntityManagerFactory emf;
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDAO.class);

    /**
     * Private constructor used for singleton pattern.
     *
     * @param emf EntityManagerFactory instance
     */
    DishDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Get the singleton instance of {@link DishDAO}.
     *
     * @param emf EntityManagerFactory for persistence context
     * @return DishDAO singleton instance
     */
    public static DishDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new DishDAO(emf);
        }
        return instance;
    }

    /**
     * Fetch a dish by its unique ID.
     *
     * @param id the dish ID
     * @return Optional containing DishDTO if found, or empty
     */
    @Override
    public Optional<DishDTO> get(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish).map(DishDTO::new);
        } catch (Exception e) {
            LOGGER.error("Failed to get dish with ID {}", id, e);
            throw e;
        }
    }

    /**
     * Fetch all dishes in the database.
     *
     * @return List of DishDTO
     */
    @Override
    public List<DishDTO> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d", DishDTO.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Failed to get all dishes", e);
            throw e;
        }
    }

    /**
     * Persist a new dish to the database.
     *
     * @param dto DishDTO with the input data
     * @return newly created DishDTO with generated ID
     */
    @Override
    public DishDTO create(DishDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = new Dish(dto);
            em.getTransaction().begin();
            em.persist(dish);
            em.flush();
            em.getTransaction().commit();
            return new DishDTO(dish);
        } catch (Exception e) {
            LOGGER.error("Failed to create dish", e);
            throw e;
        }
    }

    /**
     * Update all fields of a dish with new data.
     *
     * @param id  dish ID to update
     * @param dto DTO containing updated values
     * @return updated DishDTO, or null if not found
     */
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
        } catch (Exception e) {
            LOGGER.error("Failed to update dish with ID {}", id, e);
            throw e;
        }
    }

    /**
     * Delete a dish from the database.
     *
     * @param id the dish ID to remove
     * @return true if the dish was found and deleted, false otherwise
     */
    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            if (dish == null) return false;

            em.getTransaction().begin();
            em.remove(dish);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to delete dish with ID {}", id, e);
            throw e;
        }
    }

    /**
     * Filter dishes by their {@link DishStatus}.
     *
     * @param status dish status to filter by
     * @return List of matching DishDTOs
     */
    public List<DishDTO> getDishesByStatus(DishStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status",
                    DishDTO.class);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    /**
     * Filter dishes by allergen.
     *
     * @param allergen allergen type
     * @return List of matching DishDTOs
     */
    public List<DishDTO> getDishesByAllergen(Allergens allergen) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.allergens = :allergen",
                    DishDTO.class);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        }
    }

    /**
     * Filter dishes by both {@link DishStatus} and {@link Allergens}.
     *
     * @param status   dish status
     * @param allergen allergen type
     * @return List of matching dishes
     */
    public List<DishDTO> getDishesByStatusAndAllergen(DishStatus status, Allergens allergen) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<DishDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM Dish d WHERE d.status = :status AND d.allergens = :allergen",
                    DishDTO.class);
            query.setParameter("status", status);
            query.setParameter("allergen", allergen);
            return query.getResultList();
        }
    }

    /**
     * Partially update a single field on a dish.
     *
     * @param id    ID of the dish
     * @param field name of the field to update (e.g., "kcal")
     * @param value new value to assign
     * @return Optional with updated DishDTO, or empty if dish not found
     */
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
                        throw new IllegalArgumentException("Expected Set<Allergens> as input");
                    }
                }


                case "availableFrom" -> dish.setAvailableFrom((LocalDate) value);
                case "availableUntil" -> dish.setAvailableUntil((LocalDate) value);
                default -> throw new IllegalArgumentException("Unsupported field: " + field);
            }

            em.merge(dish);
            em.getTransaction().commit();
            return Optional.of(new DishDTO(dish));
        } catch (Exception e) {
            LOGGER.error("Failed to patch field '{}' on dish ID {}", field, id, e);
            throw e;
        }
    }

    /**
     * Utility method for converting numeric objects to {@code double}.
     *
     * @param value the input value
     * @return casted double value
     * @throws IllegalArgumentException if value is not a number
     */
    private double castDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("Expected numeric value but got: " + value);
    }

    /**
     * Creates and persists a new {@link Dish} entity including its associated {@link Recipe}
     * and {@link Ingredient} entries if provided in the {@link DishDTO}.
     * <p>
     * This method supports nested object creation, where a recipe can contain a list of ingredients.
     * It ensures correct bidirectional linkage between dish and recipe.
     * </p>
     *
     * @param dto the {@link DishDTO} containing all necessary dish, recipe, and ingredient data
     * @return a {@link DishDTO} representing the newly created dish, including its generated ID
     * @throws RuntimeException if the transaction fails or input is invalid
     */
    public DishDTO createWithRecipeAndIngredients(DishDTO dto) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // 1. Build dish from DTO
            Dish dish = new Dish(dto);

            // 2. Handle recipe if present
            if (dto.getRecipe() != null) {
                RecipeDTO recipeDTO = dto.getRecipe();
                Recipe recipe = new Recipe();
                recipe.setTitle(recipeDTO.getTitle());
                recipe.setInstructions(recipeDTO.getInstructions());

                // 3. Map ingredients from DTO if present
                if (recipeDTO.getIngredients() != null) {
                    recipe.setIngredients(
                            recipeDTO.getIngredients().stream()
                                    .map(i -> new Ingredient(i.getName(), recipe))
                                    .collect(Collectors.toSet())
                    );
                }

                // 4. Link both sides of the one-to-one relation
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

    /**
     * Updates the allergens and recipe (including ingredients) for a specific dish.
     *
     * @param dishId    the ID of the dish to update
     * @param allergens the new set of allergens
     * @param recipeDTO the new recipe data including ingredients
     * @return the updated {@link DishDTO}, or null if not found
     */
    public DishDTO updateDishRecipeAndAllergens(int dishId, Set<Allergens> allergens, RecipeDTO recipeDTO) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Dish dish = em.find(Dish.class, dishId);

            if (dish == null) {
                return null;
            }

            // Update allergens
            dish.setAllergens(allergens);

            // Replace recipe
            Recipe recipe = dish.getRecipe();
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setDish(dish);
                dish.setRecipe(recipe);
                em.persist(recipe);

            } else {
                recipe = dish.getRecipe();
            }

            recipe.setTitle(recipeDTO.getTitle());
            recipe.setInstructions(recipeDTO.getInstructions());

            // Remove old ingredients
            recipe.getIngredients().clear();
            em.flush(); // ensures orphan removal

            // Add new ingredients
            if (recipeDTO.getIngredients() != null) {
                final Recipe finalRecipe = recipe;
                Set<Ingredient> newIngredients = recipeDTO.getIngredients().stream()
                        .map(i -> new Ingredient(i.getName(), finalRecipe))
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

}