package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Ingredient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing a single ingredient.
 * Used for transferring ingredient data between client and server,
 * especially in relation to recipes.
 */
@Getter
@Setter
@NoArgsConstructor
public class IngredientDTO {

    /**
     * Unique identifier for the ingredient (may be null for new ingredients).
     */
    private Integer id;

    /**
     * Name or label of the ingredient (e.g., "Salt", "Carrot").
     */
    private String name;

    /**
     * Constructs an IngredientDTO with only a name (e.g. for creation).
     *
     * @param name the name of the ingredient
     */
    public IngredientDTO(String name) {
        this.name = name;
    }

    /**
     * Constructs an IngredientDTO from a JPA Ingredient entity.
     *
     * @param ingredient the Ingredient entity to map from
     */
    public IngredientDTO(Ingredient ingredient) {
        if (ingredient != null) {
            this.id = ingredient.getId();
            this.name = ingredient.getName();
        }
    }
}
