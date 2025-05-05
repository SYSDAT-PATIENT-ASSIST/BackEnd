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
    private Set<Ingredients> ingredients;

    /**
     * Dish associated with this recipe (optional).
     */
    @OneToOne(mappedBy = "recipe", optional = true)
    private Dish dish;

    /**
     * Cooking or preparation instructions for the dish.
     */
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    /**
     * Full constructor for initializing a Recipe entity.
     *
     * @param id           the unique ID
     * @param title        the recipe title
     * @param ingredients  set of ingredients
     * @param dish         the linked dish (optional)
     * @param instructions step-by-step instructions
     */
    public Recipe(Integer id, String title, Set<Ingredients> ingredients, Dish dish, String instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.dish = dish;
        this.instructions = instructions;
    }
}
