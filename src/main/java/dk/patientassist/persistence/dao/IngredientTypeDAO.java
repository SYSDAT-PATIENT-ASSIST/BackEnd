package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.ent.IngredientType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

/**
 * DAO to ensure uniqueness of IngredientType across recipes.
 */
public class IngredientTypeDAO {

    private final EntityManagerFactory emf;

    public IngredientTypeDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Finds an existing IngredientType by name, or creates it if not found.
     *
     * @param name the name of the ingredient type
     * @param em   the active EntityManager
     * @return the managed IngredientType instance
     */
    public IngredientType findOrCreate(String name, EntityManager em) {
        TypedQuery<IngredientType> query = em.createQuery(
                "SELECT i FROM IngredientType i WHERE i.name = :name", IngredientType.class);
        query.setParameter("name", name.trim());

        return query.getResultStream().findFirst().orElseGet(() -> {
            IngredientType type = new IngredientType(name.trim());
            em.persist(type);
            return type;
        });
    }
}
