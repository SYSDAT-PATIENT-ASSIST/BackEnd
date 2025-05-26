package dk.patientassist.control;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.dao.RecipeDAO;
import dk.patientassist.persistence.dto.RecipeDTO;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Optional;

/**
 * Controller for managing recipe-related endpoints.
 */
public class RecipeController {

    private final RecipeDAO recipeDAO;

    public RecipeController() {
        this.recipeDAO = RecipeDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    public RecipeController(RecipeDAO recipeDAO) {
        this.recipeDAO = recipeDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/recipes", this::getAllRecipes);
        app.get("/api/recipes/{id}", this::getRecipeById);
        app.post("/api/recipes", this::createRecipe);
        app.put("/api/recipes/{id}", this::updateRecipe);
        app.delete("/api/recipes/{id}", this::deleteRecipe);

        // Custom routes
        app.get("/api/recipes/by-dish/{dishId}", this::getByDishId);
        app.get("/api/recipes/search", this::searchByTitle);
    }

    public void getAllRecipes(Context ctx) {
        ctx.json(recipeDAO.getAll());
    }

    public void getRecipeById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Optional<RecipeDTO> recipe = recipeDAO.get(id);
        recipe.ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Recipe not found"));
    }

    public void createRecipe(Context ctx) {
        RecipeDTO dto = ctx.bodyAsClass(RecipeDTO.class);
        RecipeDTO created = recipeDAO.create(dto);
        ctx.status(201).json(created);
    }

    public void updateRecipe(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        RecipeDTO dto = ctx.bodyAsClass(RecipeDTO.class);
        RecipeDTO updated = recipeDAO.update(id, dto);
        if (updated != null) {
            ctx.json(updated);
        } else {
            ctx.status(404).result("Recipe not found");
        }
    }

    public void deleteRecipe(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean deleted = recipeDAO.delete(id);
        ctx.status(deleted ? 200 : 404);
    }

    public void getByDishId(Context ctx) {
        int dishId = Integer.parseInt(ctx.pathParam("dishId"));
        recipeDAO.getByDishId(dishId)
                .ifPresentOrElse(ctx::json, () -> ctx.status(404).result("No recipe for that dish"));
    }

    public void searchByTitle(Context ctx) {
        String query = ctx.queryParam("q");
        if (query == null || query.isBlank()) {
            ctx.status(400).result("Missing query parameter");
        } else {
            ctx.json(recipeDAO.searchByTitle(query));
        }
    }
}
