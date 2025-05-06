package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a unique ingredient definition, such as "Gulerod", "Tomat", or "Smør".
 * This acts as a catalog of possible ingredient types, ensuring uniqueness and reuse.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredient_type", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class IngredientType {

    /**
     * Unique ID of the ingredient type (primary key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The unique name of the ingredient, e.g. "Gulerod", "Smør".
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Constructs an ingredient type with a given name.
     *
     * @param name the name of the ingredient type
     */
    public IngredientType(String name) {
        this.name = name;
    }
}
