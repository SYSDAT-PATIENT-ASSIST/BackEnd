package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.ent.*;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utility class used to populate the development database with test data.
 * This includes users, roles, ingredients, ingredient types, dishes, recipes, and orders.
 * <p>
 * The goal is to provide consistent sample data for development and testing purposes.
 */
public class PopulateDatabase {

    static {
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    /**
     * A list of unique ingredient names used to populate {@link IngredientType} entities.
     */
    private static final List<String> ingredientNames = List.of(
            "Gulerod", "Kartofler", "Tomat", "Smør", "Mælk", "Løg", "Hvidløg", "Persille",
            "Kylling", "Rejer", "Ris", "Æg", "Rugbrød", "Citron", "Dild", "Pasta",
            "Oksekød", "Karry", "Lasagneplader", "Squash", "Aubergine", "Bechamelsauce",
            "Revet ost", "Spidskommen", "Bouillon"
    );

    /**
     * Entry point to trigger database population.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Main method for inserting demo data into the database.
     * This includes:
     * <ul>
     *     <li>Users with hashed passwords and roles</li>
     *     <li>Ingredient types with unique names</li>
     *     <li>Dishes, each with a recipe and ingredients</li>
     *     <li>Orders linked to dishes</li>
     * </ul>
     */
    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // --- Users with roles ---
            createUser("Læge", "1234", "LÆGE", em);
            createUser("Sygeplejerske", "1234", "SYGEPLEJERSKE", em);
            createUser("Kok", "1234", "KOK", em);
            createUser("Hovedkok", "1234", "HOVEDKOK", em);
            createUser("Køkken", "1234", "KØKKENPERSONALE", em);

            // --- Ingredient Types ---
            Map<String, IngredientType> ingredientTypeMap = new HashMap<>();
            for (String name : ingredientNames) {
                IngredientType type = new IngredientType(name);
                em.persist(type);
                ingredientTypeMap.put(name, type);
            }

            // --- Example Dish: --
            Dish dish = new Dish("Spaghetti med kødsovs", "Spaghetti med oksekød og tomatsauce",
                    LocalDate.now(), LocalDate.now().plusDays(7), DishStatus.TILGÆNGELIG);
            dish.setKcal(550);
            dish.setProtein(28);
            dish.setCarbohydrates(60);
            dish.setFat(20);
            dish.setAllergens(Set.of(Allergens.GLUTEN));
            em.persist(dish);

            // --- Recipe for the dish ---
            Recipe recipe = new Recipe();
            recipe.setTitle("Spaghetti med kødsovs");
            recipe.setInstructions("Brun kød, tilsæt tomatsauce og krydderier. Kog spaghetti og server.");
            recipe.setDish(dish);
            dish.setRecipe(recipe);
            em.persist(recipe);

            // --- Ingredients using predefined IngredientTypes ---
            Set<Ingredient> ingredients = Set.of(
                    new Ingredient(ingredientTypeMap.get("Pasta"), recipe),
                    new Ingredient(ingredientTypeMap.get("Oksekød"), recipe),
                    new Ingredient(ingredientTypeMap.get("Tomat"), recipe),
                    new Ingredient(ingredientTypeMap.get("Løg"), recipe),
                    new Ingredient(ingredientTypeMap.get("Hvidløg"), recipe),
                    new Ingredient(ingredientTypeMap.get("Spidskommen"), recipe)
            );
            ingredients.forEach(em::persist);
            recipe.setIngredients(ingredients);

            // --- Orders ---
            em.persist(new Order(1, LocalDateTime.now(), "Ekstra tomat", dish, OrderStatus.VENTER));
            em.persist(new Order(2, LocalDateTime.now(), "Ingen hvidløg", dish, OrderStatus.BEKRÆFTET));

            em.getTransaction().commit();
            System.out.println("✅ Database populated successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Creates a user with the specified credentials and role.
     * If the role does not exist in the database, it will be created and persisted.
     *
     * @param username the user's username
     * @param password the user's plaintext password (will be encrypted)
     * @param roleName the role to assign
     * @param em       active EntityManager for persistence context
     */
    private static void createUser(String username, String password, String roleName, EntityManager em) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        Role role = em.find(Role.class, roleName);
        if (role == null) {
            role = new Role(roleName);
            em.persist(role);
        }

        user.addRole(role);
        em.persist(user);
    }
}
