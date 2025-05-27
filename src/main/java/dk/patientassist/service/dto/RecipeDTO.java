package dk.patientassist.service.dto;

import dk.patientassist.persistence.ent.Recipe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object (DTO) representing a recipe.
 * Used for reading and writing recipe data, including its ingredients.
 */
@Getter
@Setter
@NoArgsConstructor
public class RecipeDTO {

    /**
     * Unique identifier for the recipe.
     */
    private Integer id;

    /**
     * Title or name of the recipe.
     */
    private String title;

    /**
     * Cooking or preparation instructions for the recipe.
     */
    private String instructions;

    /**
     * List of ingredients associated with the recipe.
     */
    private List<IngredientDTO> ingredients;

    /**
     * Constructor used to create a recipe DTO manually.
     *
     * @param title        the title of the recipe
     * @param instructions the preparation instructions
     * @param ingredients  list of ingredient DTOs
     */
    public RecipeDTO(String title, String instructions, List<IngredientDTO> ingredients) {
        this.title = title;
        this.instructions = instructions;
        this.ingredients = ingredients;
    }

    /**
     * Constructs a RecipeDTO from a JPA Recipe entity.
     *
     * @param recipe the recipe entity to map from
     */
    public RecipeDTO(Recipe recipe) {
        if (recipe != null) {
            this.id = recipe.getId();
            this.title = recipe.getTitle();
            this.instructions = recipe.getInstructions();
            if (recipe.getIngredients() != null) {
                this.ingredients = recipe.getIngredients().stream()
                        .map(IngredientDTO::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
