package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Ingredient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing a single ingredient.
 * Used to transfer ingredient data between client and server,
 * particularly in relation to {@link dk.patientassist.persistence.ent.Recipe} entities.
 */
@Getter
@Setter
@NoArgsConstructor
public class IngredientDTO {

    /**
     * Unique identifier for the ingredient (can be null for creation).
     */
    private Integer id;

    /**
     * The name or label of the ingredient (e.g., "Salt", "Carrot").
     * This maps to an {@link dk.patientassist.persistence.ent.IngredientType}.
     */
    private String name;

    /**
     * Constructs an IngredientDTO with a given name.
     * Used for incoming POST/PUT requests when ID is not yet known.
     *
     * @param name the name of the ingredient
     */
    public IngredientDTO(String name) {
        this.name = name;
    }

    /**
     * Constructs an IngredientDTO from a JPA {@link Ingredient} entity.
     * Extracts the name from the related {@link dk.patientassist.persistence.ent.IngredientType}.
     *
     * @param ingredient the Ingredient entity to convert
     */
    public IngredientDTO(Ingredient ingredient) {
        if (ingredient != null) {
            this.id = ingredient.getId();
            this.name = ingredient.getType() != null ? ingredient.getType().getName() : null;
        }
    }
}
