package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * JPA entity representing a recipe.
 * A recipe belongs to one dish and contains a set of ingredients and cooking instructions.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "recipe")
public class Recipe {

    /**
     * Unique identifier for the recipe.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    /**
     * Title of the recipe (e.g., "Classic Danish Frikadeller").
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Ingredients used in the recipe.
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ingredient> ingredients;

    /**
     * Cooking or preparation instructions for the dish.
     */
    @Column(name = "instructions", columnDefinition = "TEXT", nullable = false)
    private String instructions;

    /**
     * Dish associated with this recipe.
     */
    @OneToOne
    @JoinColumn(name = "dish_id") // FK managed on this side
    private Dish dish;

    /**
     * Full constructor excluding dish reference (set separately if needed).
     *
     * @param title        the title of the recipe
     * @param ingredients  ingredients used
     * @param instructions preparation steps
     */
    public Recipe(String title, Set<Ingredient> ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }
}
