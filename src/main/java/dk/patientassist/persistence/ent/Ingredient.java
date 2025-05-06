package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single ingredient used in a {@link Recipe}.
 * Each ingredient has a name and is linked to one recipe.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ingredient")
public class Ingredient {

    /**
     * Unique ID for the ingredient (primary key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    /**
     * Name of the ingredient (e.g., "Carrot", "Salt").
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The recipe this ingredient belongs to.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Constructs a new Ingredient with name and parent recipe.
     *
     * @param name   the ingredient name
     * @param recipe the recipe it belongs to
     */
    public Ingredient(String name, Recipe recipe) {
        this.name = name;
        this.recipe = recipe;
    }
}
