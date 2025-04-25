package dk.patientassist.persistence.ent;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Slide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
}
