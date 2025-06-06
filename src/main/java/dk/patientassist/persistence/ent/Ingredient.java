package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the usage of a specific {@link IngredientType} within a {@link Recipe}.
 * Models the many-to-one relationship between ingredients and both recipes and ingredient types.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredient")
public class Ingredient {

    /**
     * Auto-generated unique identifier for the ingredient entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Reference to the actual ingredient type (e.g., "Salt", "Gulerod").
     * Multiple Ingredient entries may share the same type.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_type_id", nullable = false)
    private IngredientType type;

    /**
     * The recipe this ingredient is associated with.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Constructs a new Ingredient instance linking a type and a recipe.
     *
     * @param type   the ingredient type (must not be null)
     * @param recipe the recipe this ingredient is part of (must not be null)
     */
    public Ingredient(IngredientType type, Recipe recipe) {
        this.type = type;
        this.recipe = recipe;
    }
}
