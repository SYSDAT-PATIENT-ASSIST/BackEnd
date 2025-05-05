package dk.patientassist.utilities;

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
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for populating the database with development or test data.
 * Includes sample users, dishes, recipes, ingredients, and orders.
 */
public class PopulateDatabase {

    static {
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    /**
     * Entry point to populate the database.
     * Runs the {@link #populateDatabase()} method.
     *
     * @param args CLI arguments (unused)
     */
    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Populates the database with initial demo data.
     * Includes one user, two dishes, one recipe, two ingredients, and one order.
     */
    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Create sample user
            User user = new User();
            user.setUsername("kok1");
            user.setPassword("kok1234");
            user.setRole(Role.HOVEDKOK);
            em.persist(user);

            // Add first dish
            Dish d1 = addDish(em,
                    "Frikadeller",
                    "Med brun sovs og kartofler",
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2),
                    DishStatus.UDSOLGT,
                    500, 25, 40, 20,
                    Allergens.ÆG
            );

            // Add second dish
            Dish d2 = addDish(em,
                    "Stegt flæsk",
                    "Med persillesovs og kartofler",
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    DishStatus.TILGÆNGELIG,
                    600, 35, 30, 30,
                    Allergens.SULFITTER
            );

            // Create shared recipe
            Recipe recipe = new Recipe();
            recipe.setTitle("Traditionel dansk ret");
            recipe.setInstructions("Steg grundigt og server varm.");
            recipe.setDish(d2);  // Linking recipe to dish
            em.persist(recipe);

            // Link recipe to dish
            d2.setRecipe(recipe);

            // Add ingredients
            Ingredients i1 = new Ingredients();
            i1.setName("Flæsk");
            i1.setRecipe(recipe);
            Ingredients i2 = new Ingredients();
            i2.setName("Kartofler");
            i2.setRecipe(recipe);

            Set<Ingredients> ingredientsSet = new HashSet<>();
            ingredientsSet.add(i1);
            ingredientsSet.add(i2);
            recipe.setIngredients(ingredientsSet);  // Setting ingredients to recipe

            em.persist(i1);
            em.persist(i2);

            // Add order
            Order order = new Order();
            order.setBed_id(101);
            order.setOrder_time(LocalDateTime.now());
            order.setNote("Ingen løg, tak.");
            order.setDish(d1);  // Assigning the dish to the order
            order.setStatus(OrderStatus.VENTER);
            em.persist(order);

            em.getTransaction().commit();
            System.out.println("✅ Database populated successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Helper method to create and persist a {@link Dish} entity.
     *
     * @param em          EntityManager instance
     * @param name        name of the dish
     * @param description description of the dish
     * @param from        start availability date
     * @param until       end availability date
     * @param status      availability status (e.g., AVAILABLE)
     * @param kcal        kilocalories
     * @param protein     protein content
     * @param carbs       carbohydrates content
     * @param fat         fat content
     * @param allergens   allergens (e.g., NUTS, EGGS)
     * @return the persisted Dish entity
     */
    private static Dish addDish(EntityManager em, String name, String description, LocalDate from, LocalDate until,
                                DishStatus status, double kcal, double protein, double carbs, double fat, Allergens allergens) {

        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setAvailableFrom(from);
        dish.setAvailableUntil(until);
        dish.setStatus(status);
        dish.setKcal(kcal);
        dish.setProtein(protein);
        dish.setCarbohydrates(carbs);
        dish.setFat(fat);

        dish.setAllergens(allergens != null ? allergens : Allergens.GLUTEN); // fallback

        em.persist(dish);
        return dish;
    }
}
