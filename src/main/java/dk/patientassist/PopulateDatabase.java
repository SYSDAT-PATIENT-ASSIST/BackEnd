package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Ingredients;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.ent.Recipe;
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

public class PopulateDatabase {

    static {
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args) {
        populateDatabase();
    }

    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Users with hashed passwords and roles
            User laege = createUser("laege1", "laege1234", "LÆGE", em);
            User nurse = createUser("sygeplejerske1", "nurse123", "SYGEPLEJERSKE", em);
            User kok = createUser("kok1", "kok1234", "KOK", em);
            User hovedkok = createUser("hovedkok1", "hoved123", "HOVEDKOK", em);
            User kitchen = createUser("kitchen1", "kitchen1234", "KØKKENPERSONALE", em);

            em.persist(laege);
            em.persist(nurse);
            em.persist(kok);
            em.persist(hovedkok);
            em.persist(kitchen);

            // Dishes
            Dish d1 = new Dish("Frikadeller", "Med brun sovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusWeeks(2), DishStatus.UDSOLGT);
            d1.setKcal(500);
            d1.setProtein(25);
            d1.setCarbohydrates(40);
            d1.setFat(20);
            d1.setAllergens(Allergens.ÆG);
            em.persist(d1);

            Dish d2 = new Dish("Stegt flæsk", "Med persillesovs og kartofler",
                    LocalDate.now(), LocalDate.now().plusDays(10), DishStatus.TILGÆNGELIG);
            d2.setKcal(600);
            d2.setProtein(35);
            d2.setCarbohydrates(30);
            d2.setFat(30);
            d2.setAllergens(Allergens.SULFITTER);
            em.persist(d2);

            // Recipe and Ingredients
            Recipe recipe = new Recipe(null, "Traditionel dansk ret", new HashSet<>(), d2, "Steg grundigt og server varm.");
            d2.setRecipe(recipe); // bidirectional
            em.persist(recipe);

            Ingredients i1 = new Ingredients(null, "Flæsk", recipe);
            Ingredients i2 = new Ingredients(null, "Kartofler", recipe);
            recipe.getIngredients().add(i1);
            recipe.getIngredients().add(i2);
            em.persist(i1);
            em.persist(i2);

            // Orders
            em.persist(new Order(101, LocalDateTime.now(), "Ingen løg, tak.", d1, OrderStatus.VENTER));
            em.persist(new Order(102, LocalDateTime.now(), "Ekstra brun sovs", d2, OrderStatus.BEKRÆFTET));
            em.persist(new Order(103, LocalDateTime.now(), "Uden kartofler", d2, OrderStatus.VENTER));
            em.persist(new Order(104, LocalDateTime.now(), "Skal være varm", d1, OrderStatus.AFSENDT));

            em.getTransaction().commit();
            System.out.println("✅ Database populated successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

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