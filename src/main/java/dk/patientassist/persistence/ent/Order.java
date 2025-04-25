package dk.patientassist.persistence.ent;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
@Table(name = "_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "order_dish", joinColumns = @JoinColumn(name = "dish_id"), inverseJoinColumns = @JoinColumn(name = "order_id"))
    public Set<Order> orders;
}
