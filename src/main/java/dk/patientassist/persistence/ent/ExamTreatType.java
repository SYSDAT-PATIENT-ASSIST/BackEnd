package dk.patientassist.persistence.ent;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * IllnessCategory
 */
@Entity
public class ExamTreatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    // @Column(unique = true)
    public String name;
    public String description;
    @ManyToOne
    @JoinColumn(name = "exam_treatment_category")
    public ExamTreatCategory examTreatCategory;
    @OneToMany(mappedBy = "examTreatType", cascade = CascadeType.ALL)
    public Set<ExamTreat> examTreats;
}
