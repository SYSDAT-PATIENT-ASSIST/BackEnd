package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a unique ingredient definition, such as "Gulerod", "Tomat", or "Smør".
 * <p>
 * IngredientType acts as a catalog entry that can be reused across multiple {@link Ingredient} entities.
 * This prevents duplication of common ingredient names across recipes.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredient_type", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class IngredientType {

    /**
     * Auto-generated unique identifier for this ingredient type.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The unique name of the ingredient (e.g., "Gulerod", "Smør").
     * Must not be null and must be unique in the database.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Constructs a new IngredientType with the specified name.
     *
     * @param name the name of the ingredient type (must not be null or blank)
     */
    public IngredientType(String name) {
        this.name = name;
    }
}
