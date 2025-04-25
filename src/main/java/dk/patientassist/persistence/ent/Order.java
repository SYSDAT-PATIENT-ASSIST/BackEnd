package dk.patientassist.persistence.ent;

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
}
