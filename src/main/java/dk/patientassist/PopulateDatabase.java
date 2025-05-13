package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.IngredientDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
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
import java.util.stream.Collectors;

/**
 * Utility class used to populate the development database with test data.
 * This includes users, roles, ingredients, ingredient types, dishes, recipes, and orders.
 */
public class PopulateDatabase {

    static {
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    private static final List<String> ingredientNames = List.of(
            "Gulerod", "Kartofler", "Tomat", "Smør", "Mælk", "Løg", "Hvidløg", "Persille",
            "Kylling", "Rejer", "Ris", "Æg", "Rugbrød", "Citron", "Dild", "Pasta",
            "Oksekød", "Karry", "Lasagneplader", "Squash", "Aubergine", "Bechamelsauce",
            "Revet ost", "Spidskommen", "Bouillon"
    );

    public static void main(String[] args) {
        populateDatabase();
        System.out.println("Database populated with test data.");
    }

    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // --- Users with roles ---
            createUser("læge", "1234", "DOCTOR", em);
            createUser("sygeplejerske", "1234", "NURSE", em);
            createUser("kok", "1234", "CHEF", em);
            createUser("hovedkok", "1234", "HEAD_CHEF", em);
            createUser("køkkenpersonale", "1234", "KITCHEN_STAFF", em);

            // --- Ingredient Types ---
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

            // --- Example Dishes ---
            List<Dish> dishes = new ArrayList<>();

            dishes.add(createDishWithRecipe(
                    em, "Pasta Bolognese", "Med oksekød og tomatsauce",
                    LocalDate.now(), 5, DishStatus.TILGÆNGELIG, Set.of(Allergens.GLUTEN),
                    "Pasta med kødsovs",
                    "Brun oksekød, tilsæt tomat og krydderier, kog pasta.",
                    List.of("Pasta", "Oksekød", "Tomat", "Løg", "Hvidløg")));

            dishes.add(createDishWithRecipe(
                    em, "Kylling i karry", "Serveres med ris",
                    LocalDate.now(), 7, DishStatus.TILGÆNGELIG, Set.of(Allergens.SELLERI),
                    "Karrykylling med ris",
                    "Steg kylling, tilsæt karry og kokosmælk. Server med ris.",
                    List.of("Kylling", "Karry", "Løg", "Ris", "Bouillon")));

            dishes.add(createDishWithRecipe(
                    em, "Grøntsagslasagne", "Vegetarlasagne med ost",
                    LocalDate.now(), 10, DishStatus.TILGÆNGELIG, Set.of(Allergens.GLUTEN, Allergens.LAKTOSE),
                    "Ovnbagt grøntsagslasagne",
                    "Lag squash, aubergine, tomat og ost. Bag ved 200°C i 45 min.",
                    List.of("Lasagneplader", "Squash", "Aubergine", "Tomat", "Revet ost", "Bechamelsauce")));

            dishes.add(createDishWithRecipe(
                    em, "Rugbrød med æg og rejer", "Klassisk dansk smørrebrød",
                    LocalDate.now(), 4, DishStatus.TILGÆNGELIG, Set.of(Allergens.GLUTEN, Allergens.SKALDYR, Allergens.ÆG),
                    "Smørrebrød",
                    "Kog æg, læg på rugbrød med rejer og pynt.",
                    List.of("Rugbrød", "Æg", "Rejer", "Citron", "Dild", "Smør")));

            // --- Orders ---
            em.persist(new Order(1, LocalDateTime.now(), "Ingen hvidløg", dishes.get(0), OrderStatus.VENTER));
            em.persist(new Order(2, LocalDateTime.now(), "Ekstra ris", dishes.get(1), OrderStatus.BEKRÆFTET));
            em.persist(new Order(3, LocalDateTime.now(), "Ingen squash", dishes.get(2), OrderStatus.VENTER));
            em.persist(new Order(4, LocalDateTime.now(), "Uden citron", dishes.get(3), OrderStatus.BEKRÆFTET));
            em.persist(new Order(5, LocalDateTime.now(), "Ekstra ost", dishes.get(2), OrderStatus.AFSENDT));
            em.persist(new Order(6, LocalDateTime.now(), "Skal være varm", dishes.get(0), OrderStatus.VENTER));

            em.getTransaction().commit();
            System.out.println("✅ Database populated successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

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

    private static Dish createDishWithRecipe(EntityManager em, String name, String description,
                                             LocalDate from, int daysAvailable, DishStatus status,
                                             Set<Allergens> allergens, String recipeTitle, String instructions,
                                             List<String> ingredientNames) {
        Dish dish = new Dish(name, description, from, from.plusDays(daysAvailable), status);
        dish.setKcal(400 + new Random().nextInt(200));
        dish.setProtein(20 + new Random().nextInt(10));
        dish.setCarbohydrates(30 + new Random().nextInt(20));
        dish.setFat(10 + new Random().nextInt(10));
        dish.setAllergens(allergens);
        em.persist(dish);

        Recipe recipe = new Recipe();
        recipe.setTitle(recipeTitle);
        recipe.setInstructions(instructions);
        recipe.setDish(dish);
        dish.setRecipe(recipe);
        em.persist(recipe);

        Set<Ingredient> ingredients = ingredientNames.stream()
                .map(nameStr -> new Ingredient(findIngredientTypeByName(em, nameStr), recipe))
                .collect(Collectors.toSet());
        ingredients.forEach(em::persist);
        recipe.setIngredients(ingredients);

        return dish;
    }

    private static IngredientType findIngredientTypeByName(EntityManager em, String name) {
        return em.createQuery("SELECT i FROM IngredientType i WHERE i.name = :name", IngredientType.class)
                .setParameter("name", name)
                .getSingleResult();
    }
}
