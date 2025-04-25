package dk.patientassist.persistence.ent;

import dk.patientassist.persistence.enums.AgeGroup;
import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class GameCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @Enumerated(EnumType.STRING)
    public AgeGroup ageGroup;
}
