package dk.patientassist.persistence.ent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Patient Assist
 */
@Entity
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column(nullable = false)
    public Boolean occupied;
    @Column(name = "patient_name", nullable = true)
    public String patientName;
    @ManyToOne
    @JoinColumn(name = "section_id")
    public Section section;
}
