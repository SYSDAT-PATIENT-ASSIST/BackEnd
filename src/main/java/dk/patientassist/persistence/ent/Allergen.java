package dk.patientassist.persistence.ent;

import java.util.Set;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Allergen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @ManyToMany
    @JoinTable(name = "dish_allergen", joinColumns = @JoinColumn(name = "dish_id"), inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    public Set<Dish> dishes;
}
