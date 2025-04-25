package dk.patientassist.persistence.ent;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "dish_allergen", joinColumns = @JoinColumn(name = "allergen_id"), inverseJoinColumns = @JoinColumn(name = "dish_id"))
    public Set<Allergen> allergens;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "order_dish", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "dish_id"))
    public Set<Order> orders;
}
