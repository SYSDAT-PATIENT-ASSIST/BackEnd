package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Recipe;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecipeDTO {

    private Integer id;
    private String title;
    private String instructions;

    public RecipeDTO(String title, String instructions) {
        this.title = title;
        this.instructions = instructions;
    }

    public RecipeDTO(Recipe recipe) {
        if (recipe != null) {
            this.id = recipe.getId();
            this.title = recipe.getTitle();
            this.instructions = recipe.getInstructions();
        }
    }
}
