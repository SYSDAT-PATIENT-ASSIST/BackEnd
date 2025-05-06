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
 * This includes users, roles, ingredient types, dishes, recipes, and orders.
 * Ensures uniqueness for ingredient types and consistent test data setup.
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
     * @param args unused
     */
    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Inserts demo data into the database.
     * <ul>
     *     <li>Creates roles and users</li>
     *     <li>Creates or reuses existing ingredient types</li>
     *     <li>Creates a dish with recipe and ingredients</li>
     *     <li>Creates example orders</li>
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

            // --- Ingredient Types (find or create to prevent duplicates) ---
            Map<String, IngredientType> ingredientTypeMap = new HashMap<>();
            for (String name : ingredientNames) {
                IngredientType existing = em.createQuery(
                                "SELECT i FROM IngredientType i WHERE i.name = :name", IngredientType.class)
                        .setParameter("name", name)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    existing = new IngredientType(name);
                    em.persist(existing);
                }

                ingredientTypeMap.put(name, existing);
            }

            // --- Create a Dish ---
            Dish dish = new Dish("Spaghetti med kødsovs", "Spaghetti med oksekød og tomatsauce",
                    LocalDate.now(), LocalDate.now().plusDays(7), DishStatus.TILGÆNGELIG);
            dish.setKcal(550);
            dish.setProtein(28);
            dish.setCarbohydrates(60);
            dish.setFat(20);
            dish.setAllergens(Set.of(Allergens.GLUTEN));
            em.persist(dish);

            // --- Create Recipe ---
            Recipe recipe = new Recipe();
            recipe.setTitle("Spaghetti med kødsovs");
            recipe.setInstructions("Brun kød, tilsæt tomatsauce og krydderier. Kog spaghetti og server.");
            recipe.setDish(dish);
            dish.setRecipe(recipe);
            em.persist(recipe);

            // --- Add Ingredients to Recipe ---
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

            // --- Example Orders ---
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
     * Creates a new user and role (if missing), hashes the password, and persists them.
     *
     * @param username the username of the user
     * @param password the plain-text password
     * @param roleName the role to assign
     * @param em       the entity manager
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
