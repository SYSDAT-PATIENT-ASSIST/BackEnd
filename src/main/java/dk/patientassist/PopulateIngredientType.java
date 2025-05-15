package dk.patientassist;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.IngredientType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

/**
 * Utility class to populate the database with predefined {@link IngredientType}
 * entries.
 * This can be run independently to ensure a fixed set of reusable ingredients
 * is loaded into the database.
 */
public class PopulateIngredientType {

    static {
        HibernateConfig.init(Mode.DEV);
    }

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    /**
     * A predefined list of unique ingredient names to populate
     * {@link IngredientType} entries.
     */
    private static final List<String> INGREDIENT_NAMES = List.of(
            // Grøntsager
            "Gulerod", "Kartofler", "Tomat", "Løg", "Hvidløg", "Squash", "Aubergine", "Peberfrugt", "Bladselleri",
            "Spinat", "Majs", "Grønne bønner", "Broccoli", "Blomkål", "Porre", "Rødbede", "Pastinak",

            // Basisvarer & mejeriprodukter
            "Smør", "Mælk", "Fløde", "Yoghurt", "Æg", "Ost", "Revet ost", "Bechamelsauce", "Creme fraiche",

            // Korn & pasta
            "Rugbrød", "Pasta", "Lasagneplader", "Ris", "Bulgur", "Couscous", "Quinoa",

            // Kød og fisk
            "Kylling", "Oksekød", "Svinekød", "Fisk", "Torsk", "Laks", "Rejer", "Skinke", "Kødboller",

            // Bælgfrugter & planteproteiner
            "Røde linser", "Grønne linser", "Kikærter", "Tofu", "Edamame", "Sorte bønner",

            // Krydderier og smagsgivere
            "Salt", "Peber", "Karry", "Spidskommen", "Paprika", "Oregano", "Basilikum", "Timian", "Muskatnød",
            "Chili", "Laurbærblad", "Ingefær", "Citron", "Dild", "Persille",

            // Færdige produkter og andet
            "Tomatsauce", "Bouillon", "Grøntsagsbouillon", "Kokosmælk", "Pesto", "Mayonnaise", "Senep", "Ketchup",
            "Brun sovs", "Ristede løg", "Ærter");

    /**
     * Entry point to populate IngredientType table.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        populateIngredientTypes();
    }

    /**
     * Inserts the predefined list of {@link IngredientType}s into the database
     * if they do not already exist (based on name uniqueness).
     */
    public static void populateIngredientTypes() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            for (String name : INGREDIENT_NAMES) {
                // Avoid duplicate inserts if already present
                Long count = em.createQuery("SELECT COUNT(i) FROM IngredientType i WHERE i.name = :name", Long.class)
                        .setParameter("name", name)
                        .getSingleResult();
                if (count == 0) {
                    IngredientType type = new IngredientType(name);
                    em.persist(type);
                }
            }

            em.getTransaction().commit();
            System.out.println("✅ IngredientType table populated successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
