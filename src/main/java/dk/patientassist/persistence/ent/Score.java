package dk.patientassist.persistence.ent;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Patient Assist
 */
@Entity
public class Score
{
    @Id public Integer id;
    public String name;
}
