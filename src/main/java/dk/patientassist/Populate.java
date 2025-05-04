package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.ent.*;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import dk.patientassist.persistence.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utility class for populating the development database with initial test data.
 * This includes dishes, recipes, ingredients, users, and orders with varied attributes and relationships.
 * <p>
 * Intended for use in development mode only.
 */
public class Populate {

    static {
        // Initialize Hibernate in development mode
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    /**
     * Main method to execute the database population.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Populates the database with:
     * - 3 example Danish dishes with different statuses and allergens
     * - Recipes and ingredients for each dish
     * - Users with various roles
     * - Orders linked to the dishes with varied statuses
     */
    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Add example dishes
            Dish d1 = addDish(em, "Kylling i karry", "Served with rice", DishStatus.AVAILABLE, Allergens.GLUTEN);
            Dish d2 = addDish(em, "Frikadeller", "With brown gravy and potatoes", DishStatus.SOLD_OUT, Allergens.EGGS);
            Dish d3 = addDish(em, "Smørrebrød", "With egg and shrimp", DishStatus.AVAILABLE, Allergens.SHELLFISH);

            // Add test users
            User u1 = createUser(em, "chef", "chef123", Role.HEADCHEF);
            User u2 = createUser(em, "nurse", "nurse123", Role.NURSE);
            User u3 = createUser(em, "cook", "cook123", Role.CHEF);

            // Add sample orders with varied statuses
            createOrder(em, 101, d1, "No nuts please", OrderStatus.PENDING);
            createOrder(em, 102, d2, "Extra gravy", OrderStatus.COMPLETED);
            createOrder(em, 103, d3, "Low salt", OrderStatus.CANCELLED);

            em.getTransaction().commit();
            System.out.println("Database successfully populated with dishes, users, and orders!");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Creates and persists a dish, its recipe, and ingredients.
     *
     * @param em         the EntityManager used for persistence
     * @param name       the name of the dish
     * @param description the dish description
     * @param status     the status of the dish (AVAILABLE, SOLD_OUT, etc.)
     * @param allergen   primary allergen contained in the dish
     * @return the persisted Dish entity
     */
    private static Dish addDish(EntityManager em, String name, String description, DishStatus status, Allergens allergen) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setAvailable_from(LocalDate.now());
        dish.setAvailable_until(LocalDate.now().plusWeeks(2));
        dish.setStatus(status);
        dish.setKcal(500);
        dish.setProtein(25);
        dish.setCarbohydrates(40);
        dish.setFat(20);
        dish.setAllergens(allergen);
        em.persist(dish);

        Recipe recipe = new Recipe();
        recipe.setTitle(name + " Recipe");
        recipe.setInstructions("Prepare according to kitchen manual.");
        recipe.setDish(dish);
        dish.setRecipe(recipe);
        em.persist(recipe);

        Ingredients i1 = new Ingredients();
        i1.setName("Ingredient A");
        i1.setRecipe(recipe);
        Ingredients i2 = new Ingredients();
        i2.setName("Ingredient B");
        i2.setRecipe(recipe);

        recipe.setIngredients(new HashSet<>(Arrays.asList(i1, i2)));

        em.persist(i1);
        em.persist(i2);

        return dish;
    }

    /**
     * Creates and persists a user with a specific role.
     *
     * @param em       the EntityManager used for persistence
     * @param username the user's login name
     * @param password the user's password (plain text here for testing purposes)
     * @param role     the role assigned to the user (e.g., CHEF, NURSE)
     * @return the persisted User entity
     */
    private static User createUser(EntityManager em, String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        em.persist(user);
        return user;
    }

    /**
     * Creates and persists an order for a specific dish and patient bed.
     *
     * @param em     the EntityManager used for persistence
     * @param bedId  ID of the hospital bed
     * @param dish   the dish associated with the order
     * @param note   any special request notes
     * @param status the order status (PENDING, COMPLETED, etc.)
     */
    private static void createOrder(EntityManager em, int bedId, Dish dish, String note, OrderStatus status) {
        Order order = new Order();
        order.setBed_id(bedId);
        order.setOrder_time(LocalDateTime.now());
        order.setNote(note);
        order.setDish(dish);
        order.setStatus(status);
        em.persist(order);
    }
}
