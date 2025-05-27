package dk.patientassist.persistence.dao;

import dk.patientassist.service.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Recipe;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class RecipeDAO implements IDAO<RecipeDTO, Integer> {

    private static RecipeDAO instance;
    private final EntityManagerFactory emf;

    private RecipeDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static RecipeDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new RecipeDAO(emf);
        }
        return instance;
    }

    @Override
    public Optional<RecipeDTO> get(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Recipe recipe = em.find(Recipe.class, id);
            return Optional.ofNullable(recipe).map(RecipeDTO::new);
        }
    }

    @Override
    public List<RecipeDTO> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<RecipeDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.RecipeDTO(r) FROM Recipe r", RecipeDTO.class);
            return query.getResultList();
        }
    }

    @Override
    public RecipeDTO create(RecipeDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Recipe recipe = new Recipe();
            recipe.setTitle(dto.getTitle());
            recipe.setInstructions(dto.getInstructions());
            em.persist(recipe);
            em.getTransaction().commit();
            return new RecipeDTO(recipe);
        }
    }

    @Override
    public RecipeDTO update(Integer id, RecipeDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            Recipe recipe = em.find(Recipe.class, id);
            if (recipe == null) return null;

            em.getTransaction().begin();
            recipe.setTitle(dto.getTitle());
            recipe.setInstructions(dto.getInstructions());
            em.merge(recipe);
            em.getTransaction().commit();
            return new RecipeDTO(recipe);
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Recipe recipe = em.find(Recipe.class, id);
            if (recipe == null) return false;

            em.getTransaction().begin();
            em.remove(recipe);
            em.getTransaction().commit();
            return true;
        }
    }

    /**
     * Get recipe by associated Dish ID.
     * @param dishId ID of the dish
     * @return Optional of RecipeDTO
     */
    public Optional<RecipeDTO> getByDishId(Integer dishId) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Recipe> query = em.createQuery(
                    "SELECT r FROM Recipe r WHERE r.dish.id = :dishId", Recipe.class);
            query.setParameter("dishId", dishId);
            return query.getResultStream().findFirst().map(RecipeDTO::new);
        }
    }

    /**
     * Search recipes by title (case-insensitive LIKE).
     * @param keyword part of the title
     * @return list of matching RecipeDTOs
     */
    public List<RecipeDTO> searchByTitle(String keyword) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<RecipeDTO> query = em.createQuery(
                    "SELECT new dk.patientassist.persistence.dto.RecipeDTO(r) FROM Recipe r " +
                            "WHERE LOWER(r.title) LIKE LOWER(:keyword)", RecipeDTO.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        }
    }

}
