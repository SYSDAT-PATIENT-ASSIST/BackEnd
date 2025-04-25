package dk.patientassist.persistence.ent;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
}
