package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a usage of an ingredient within a specific recipe.
 * Each Ingredient is linked to a {@link Recipe} and references a unique {@link IngredientType}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredient")
public class Ingredient {

    /**
     * Unique ID of this specific ingredient entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The ingredient type (e.g. "Gulerod", "Smør") used in this recipe.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_type_id", nullable = false)
    private IngredientType type;

    /**
     * The recipe this ingredient is part of.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Constructs an ingredient used in a specific recipe.
     *
     * @param type   the ingredient type
     * @param recipe the recipe this ingredient is part of
     */
    public Ingredient(IngredientType type, Recipe recipe) {
        this.type = type;
        this.recipe = recipe;
    }
}
