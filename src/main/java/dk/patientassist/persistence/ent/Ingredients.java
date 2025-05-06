package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single ingredient used in a {@link Recipe}.
 * Each ingredient has a name and is associated with one recipe.
 * Used to model the composition of dishes in the hospital's meal system.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredients {

    /**
     * Unique ID for the ingredient (primary key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    /**
     * The name or label of the ingredient (e.g., "Carrot", "Salt").
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The recipe this ingredient belongs to.
     * Many ingredients can be part of one recipe.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Full constructor to initialize an Ingredient entity.
     *
     * @param id     the unique identifier of the ingredient
     * @param name   the ingredient name
     * @param recipe the parent recipe this ingredient belongs to
     */
    public Ingredients(Integer id, String name, Recipe recipe) {
        this.id = id;
        this.name = name;
        this.recipe = recipe;
    }
}
