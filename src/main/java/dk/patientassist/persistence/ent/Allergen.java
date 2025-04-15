package dk.patientassist.persistence.ent;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;

/**
 * Patient Assist
 */
@Entity
@EqualsAndHashCode
public class Allergen
{
    @Id public Integer id;
    public String name;
}
