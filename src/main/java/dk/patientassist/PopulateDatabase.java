package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.IngredientTypeDAO;
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

    public static void main(String[] args) {
        populateDatabase();
    }

    /**
     * Populates the database with sample data using unique ingredient types.
     */
    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();
        IngredientTypeDAO ingredientTypeDAO = new IngredientTypeDAO(emf);

        try {
            em.getTransaction().begin();

            User doctor = createUser("laege1", "laege1234", "LÆGE", em);
            em.persist(doctor);

            User nurse = createUser("sygeplejerske1", "nurse123", "SYGEPLEJERSKE", em);
            em.persist(nurse);

            User chef = createUser("kok1", "kok1234", "KOK", em);
            em.persist(chef);

            User headChef = createUser("hovedkok1", "hoved123", "HOVEDKOK", em);
            em.persist(headChef);

            User kitchenStaff = createUser("kitchen1", "kitchen1234", "KØKKENPERSONALE", em);
            em.persist(kitchenStaff);

            // Dish 1
            Dish d1 = new Dish("Frikadeller", "Med brun sovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusWeeks(2), DishStatus.UDSOLGT);
            d1.setKcal(500);
            d1.setProtein(25);
            d1.setCarbohydrates(40);
            d1.setFat(20);
            d1.setAllergens(Set.of(Allergens.ÆG));
            em.persist(d1);

            // Dish 2
            Dish d2 = new Dish("Stegt flæsk", "Med persillesovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusDays(10), DishStatus.TILGÆNGELIG);
            d2.setKcal(600);
            d2.setProtein(35);
            d2.setCarbohydrates(30);
            d2.setFat(30);
            d2.setAllergens(Set.of(Allergens.SULFITTER));
            em.persist(d2);

            // Recipe for Dish 2
            Recipe r2 = new Recipe();
            r2.setTitle("Traditionel dansk ret");
            r2.setInstructions("Steg flæsket sprødt. Kog kartofler og lav persillesovs med smør og mælk.");
            r2.setDish(d2);

            Set<Ingredient> i2 = Set.of(
                    new Ingredient(ingredientTypeDAO.findOrCreate("Flæsk", em), r2),
                    new Ingredient(ingredientTypeDAO.findOrCreate("Kartofler", em), r2),
                    new Ingredient(ingredientTypeDAO.findOrCreate("Persille", em), r2),
                    new Ingredient(ingredientTypeDAO.findOrCreate("Smør", em), r2),
                    new Ingredient(ingredientTypeDAO.findOrCreate("Mælk", em), r2)
            );

            r2.setIngredients(i2);
            d2.setRecipe(r2);
            em.persist(r2);
            i2.forEach(em::persist);

            // Orders
            em.persist(new Order(101, LocalDateTime.now(), "Ingen løg, tak.", d1, OrderStatus.VENTER));
            em.persist(new Order(102, LocalDateTime.now(), "Ekstra brun sovs", d2, OrderStatus.BEKRÆFTET));

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
     * Helper method to create a user with encrypted password and a role.
     *
     * @param username username
     * @param password raw password
     * @param roleName role name
     * @param em       entity manager
     * @return constructed user
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
