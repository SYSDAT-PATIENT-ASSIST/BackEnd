package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "recipe")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title" )
    private String title;

    @OneToMany(mappedBy = "recipe")
    @Column(name = "ingredients")
    private Set<Ingredients> ingredients;

    @OneToOne(mappedBy = "recipe")
    private Dish dish;

    @Column(name = "instructions")
    private String instructions;

    public Recipe(Integer id, String title, Set<Ingredients> ingredients, Dish dish, String instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.dish = dish;
        this.instructions = instructions;
    }
}
