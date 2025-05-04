package dk.patientassist;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.persistence.ent.*;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Populate {

    static {
        HibernateConfig.init(Mode.DEV);
    }

    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args) {
        populateDatabase();
    }

    public static void populateDatabase() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Create Dish
            Dish dish = new Dish();
            dish.setName("Chicken Salad");
            dish.setDescription("A fresh salad with grilled chicken.");
            dish.setAvailable_from(LocalDate.now());
            dish.setAvailable_until(LocalDate.now().plusMonths(1));
            dish.setStatus(DishStatus.AVAILABLE);
            dish.setKcal(350);
            dish.setProtein(30);
            dish.setCarbohydrates(15);
            dish.setFat(10);

            em.persist(dish);

            // Create Recipe
            Recipe recipe = new Recipe();
            recipe.setTitle("Chicken Salad Recipe");
            recipe.setInstructions("Grill chicken and mix with salad ingredients.");
            recipe.setDish(dish);

            em.persist(recipe);

            // Link Dish to Recipe (two-way)
            dish.setRecipe(recipe);

            // Create Ingredients
            Ingredients ingredient1 = new Ingredients();
            ingredient1.setRecipe(recipe);

            Ingredients ingredient2 = new Ingredients();
            ingredient2.setRecipe(recipe);

            Set<Ingredients> ingredientsSet = new HashSet<>();
            ingredientsSet.add(ingredient1);
            ingredientsSet.add(ingredient2);

            recipe.setIngredients(ingredientsSet);

            em.persist(ingredient1);
            em.persist(ingredient2);

            // Create User
            User user = new User();
            user.setUsername("testuser");
            user.setPassword("password123");

            em.persist(user);

            // Create Order
            Order order = new Order();
            order.setBed_id(101);
            order.setOrder_time(LocalDateTime.now());
            order.setNote("Extra dressing, please.");
            order.setDish(dish);
            order.setStatus(OrderStatus.PENDING);

            em.persist(order);

            em.getTransaction().commit();
            System.out.println("Database populated successfully! 🚀");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
