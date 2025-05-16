package dk.patientassist.persistence.ent;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * TreatmentCategory
 */
@Entity
public class ExamTreatCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column(unique = true)
    public String name;
    public String description;
    @OneToMany(mappedBy = "examTreatCategory", cascade = CascadeType.ALL)
    public Set<ExamTreatType> examTreatTypes;
}
