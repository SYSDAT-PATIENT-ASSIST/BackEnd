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
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class used to populate the development database with test data,
 * including users, roles, dishes, recipes, ingredients, and orders.
 */
public class PopulateDatabase {

    static {
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    /**
     * Entry point for database population.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Populates the database with sample data for development and testing purposes.
     * Includes:
     * - Users with roles
     * - Dishes with allergens
     * - A dish with a recipe and ingredients
     * - Orders linked to dishes
     */
    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // --- Users with roles ---
            User doctor = createUser("laege1", "laege1234", "LÆGE", em);
            User nurse = createUser("sygeplejerske1", "nurse123", "SYGEPLEJERSKE", em);
            User chef = createUser("kok1", "kok1234", "KOK", em);
            User headChef = createUser("hovedkok1", "hoved123", "HOVEDKOK", em);
            User kitchenStaff = createUser("kitchen1", "kitchen1234", "KØKKENPERSONALE", em);

            em.persist(doctor);
            em.persist(nurse);
            em.persist(chef);
            em.persist(headChef);
            em.persist(kitchenStaff);

            // --- Dishes ---
            Dish d1 = new Dish("Frikadeller", "Med brun sovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusWeeks(2), DishStatus.UDSOLGT);
            d1.setKcal(500);
            d1.setProtein(25);
            d1.setCarbohydrates(40);
            d1.setFat(20);
            d1.setAllergens(Set.of(Allergens.ÆG));
            em.persist(d1);

            Dish d2 = new Dish("Stegt flæsk", "Med persillesovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusDays(10), DishStatus.TILGÆNGELIG);
            d2.setKcal(600);
            d2.setProtein(35);
            d2.setCarbohydrates(30);
            d2.setFat(30);
            d2.setAllergens(Set.of(Allergens.SULFITTER));
            em.persist(d2);

            // --- Recipe and Ingredients for d2 ---
            Recipe recipe = new Recipe();
            recipe.setTitle("Traditionel dansk ret");
            recipe.setInstructions("Steg flæsket sprødt. Kog kartofler og lav persillesovs med smør og mælk.");

            Set<Ingredient> ingredients = Set.of(
                    new Ingredient("Flæsk", recipe),
                    new Ingredient("Kartofler", recipe),
                    new Ingredient("Persille", recipe),
                    new Ingredient("Smør", recipe),
                    new Ingredient("Mælk", recipe)
            );

            recipe.setIngredients(ingredients);
            recipe.setDish(d2); // bidirectional link
            d2.setRecipe(recipe);

            em.persist(recipe);
            ingredients.forEach(em::persist);

            // Dish 3 - Grøntsagslasagne
            Dish d3 = new Dish("Grøntsagslasagne", "Vegetarlasagne med ostesauce",
                    LocalDate.now(), LocalDate.now().plusDays(14), DishStatus.TILGÆNGELIG);
            d3.setKcal(450);
            d3.setProtein(18);
            d3.setCarbohydrates(50);
            d3.setFat(15);
            d3.setAllergens(Set.of(Allergens.LAKTOSE, Allergens.GLUTEN));
            em.persist(d3);

            Recipe r3 = new Recipe();
            r3.setTitle("Vegetarisk grøntsagslasagne");
            r3.setInstructions("Læg lag med grøntsager og ostesauce. Bag ved 200°C i 45 min.");
            Set<Ingredient> i3 = Set.of(
                    new Ingredient("Lasagneplader", r3),
                    new Ingredient("Squash", r3),
                    new Ingredient("Aubergine", r3),
                    new Ingredient("Tomatsauce", r3),
                    new Ingredient("Revet ost", r3),
                    new Ingredient("Bechamelsauce", r3)
            );
            r3.setIngredients(i3);
            r3.setDish(d3);
            d3.setRecipe(r3);
            em.persist(r3);
            i3.forEach(em::persist);

// Dish 4 - Kylling i karry
            Dish d4 = new Dish("Kylling i karry", "Serveres med ris",
                    LocalDate.now(), LocalDate.now().plusDays(10), DishStatus.TILGÆNGELIG);
            d4.setKcal(520);
            d4.setProtein(30);
            d4.setCarbohydrates(45);
            d4.setFat(22);
            d4.setAllergens(Set.of(Allergens.SELLERI));
            em.persist(d4);

            Recipe r4 = new Recipe();
            r4.setTitle("Kylling i karry med ris");
            r4.setInstructions("Steg kylling, tilsæt karry, kokosmælk og lad simre. Server med ris.");
            Set<Ingredient> i4 = Set.of(
                    new Ingredient("Kylling", r4),
                    new Ingredient("Karry", r4),
                    new Ingredient("Kokosmælk", r4),
                    new Ingredient("Løg", r4),
                    new Ingredient("Ris", r4),
                    new Ingredient("Salt", r4)
            );
            r4.setIngredients(i4);
            r4.setDish(d4);
            d4.setRecipe(r4);
            em.persist(r4);
            i4.forEach(em::persist);

// Dish 5 - Rugbrødsmad med æg og rejer
            Dish d5 = new Dish("Rugbrød med æg og rejer", "Klassisk dansk frokost",
                    LocalDate.now(), LocalDate.now().plusDays(7), DishStatus.TILGÆNGELIG);
            d5.setKcal(300);
            d5.setProtein(20);
            d5.setCarbohydrates(25);
            d5.setFat(10);
            d5.setAllergens(Set.of(Allergens.ÆG, Allergens.SKALDYR, Allergens.GLUTEN));
            em.persist(d5);

            Recipe r5 = new Recipe();
            r5.setTitle("Rugbrødsmad med æg og rejer");
            r5.setInstructions("Kog æg, anret på rugbrød med rejer, pynt med citron og dild.");
            Set<Ingredient> i5 = Set.of(
                    new Ingredient("Rugbrød", r5),
                    new Ingredient("Æg", r5),
                    new Ingredient("Rejer", r5),
                    new Ingredient("Citron", r5),
                    new Ingredient("Dild", r5),
                    new Ingredient("Smør", r5)
            );
            r5.setIngredients(i5);
            r5.setDish(d5);
            d5.setRecipe(r5);
            em.persist(r5);
            i5.forEach(em::persist);

// Dish 6 - Linsesuppe
            Dish d6 = new Dish("Linsesuppe", "Varm og krydret suppe",
                    LocalDate.now(), LocalDate.now().plusDays(8), DishStatus.TILGÆNGELIG);
            d6.setKcal(380);
            d6.setProtein(15);
            d6.setCarbohydrates(40);
            d6.setFat(12);
            d6.setAllergens(Set.of()); // ingen kendte allergener
            em.persist(d6);

            Recipe r6 = new Recipe();
            r6.setTitle("Rød linsesuppe");
            r6.setInstructions("Kog linser med grøntsager og krydderier. Blend og server.");
            Set<Ingredient> i6 = Set.of(
                    new Ingredient("Røde linser", r6),
                    new Ingredient("Gulerod", r6),
                    new Ingredient("Løg", r6),
                    new Ingredient("Hvidløg", r6),
                    new Ingredient("Spidskommen", r6),
                    new Ingredient("Grøntsagsbouillon", r6)
            );
            r6.setIngredients(i6);
            r6.setDish(d6);
            d6.setRecipe(r6);
            em.persist(r6);
            i6.forEach(em::persist);

// Dish 7 - Pasta Bolognese
            Dish d7 = new Dish("Pasta Bolognese", "Med oksekød og tomatsauce",
                    LocalDate.now(), LocalDate.now().plusDays(5), DishStatus.TILGÆNGELIG);
            d7.setKcal(550);
            d7.setProtein(28);
            d7.setCarbohydrates(60);
            d7.setFat(20);
            d7.setAllergens(Set.of(Allergens.GLUTEN));
            em.persist(d7);

            Recipe r7 = new Recipe();
            r7.setTitle("Pasta Bolognese");
            r7.setInstructions("Brun kød, tilsæt tomatsauce og krydderier. Kog pasta og server.");
            Set<Ingredient> i7 = Set.of(
                    new Ingredient("Pasta", r7),
                    new Ingredient("Hakket oksekød", r7),
                    new Ingredient("Tomatsauce", r7),
                    new Ingredient("Løg", r7),
                    new Ingredient("Hvidløg", r7),
                    new Ingredient("Basilikum", r7)
            );
            r7.setIngredients(i7);
            r7.setDish(d7);
            d7.setRecipe(r7);
            em.persist(r7);
            i7.forEach(em::persist);



            // --- Orders ---
            em.persist(new Order(101, LocalDateTime.now(), "Ingen løg, tak.", d1, OrderStatus.VENTER));
            em.persist(new Order(102, LocalDateTime.now(), "Ekstra brun sovs", d2, OrderStatus.BEKRÆFTET));
            em.persist(new Order(103, LocalDateTime.now(), "Uden kartofler", d2, OrderStatus.VENTER));
            em.persist(new Order(104, LocalDateTime.now(), "Skal være varm", d1, OrderStatus.AFSENDT));
            em.persist(new Order(105, LocalDateTime.now(), "Ekstra ost, tak.", d3, OrderStatus.VENTER));
            em.persist(new Order(106, LocalDateTime.now(), "Ingen squash", d3, OrderStatus.BEKRÆFTET));
            em.persist(new Order(107, LocalDateTime.now(), "Mild karry ønskes", d4, OrderStatus.VENTER));
            em.persist(new Order(108, LocalDateTime.now(), "Ekstra ris", d4, OrderStatus.AFSENDT));
            em.persist(new Order(109, LocalDateTime.now(), "Serveres med citronbåd", d5, OrderStatus.VENTER));
            em.persist(new Order(110, LocalDateTime.now(), "Ingen smør", d5, OrderStatus.BEKRÆFTET));
            em.persist(new Order(111, LocalDateTime.now(), "Ingen hvidløg", d6, OrderStatus.VENTER));
            em.persist(new Order(112, LocalDateTime.now(), "", d6, OrderStatus.AFSENDT));
            em.persist(new Order(113, LocalDateTime.now(), "Ekstra pasta", d7, OrderStatus.VENTER));
            em.persist(new Order(114, LocalDateTime.now(), "Uden basilikum", d7, OrderStatus.BEKRÆFTET));


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
     * Helper method to create a user with an encrypted password and assigned role.
     * If the role does not exist, it is created.
     *
     * @param username the username for the new user
     * @param password the plain-text password (will be encrypted)
     * @param roleName the name of the role to assign
     * @param em       the active EntityManager
     * @return the constructed User entity
     */
    private static User createUser(String username, String password, String roleName, EntityManager em) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        Role role = em.find(Role.class, roleName);
        if (role == null) {
            role = new Role(roleName);
            em.persist(role);
        }

        user.addRole(role);
        return user;
    }
}
