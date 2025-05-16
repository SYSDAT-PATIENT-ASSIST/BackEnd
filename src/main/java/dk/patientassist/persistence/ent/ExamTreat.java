package dk.patientassist.persistence.ent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Illness
 */
@Entity
public class ExamTreat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    // @Column(unique = true)
    public String name;
    public String description;
    @Column(name = "src_url")
    public String srcUrl;
    @Column(columnDefinition = "text")
    public String article;
    @ManyToOne
    @JoinColumn(name = "exam_treatment_type")
    public ExamTreatType examTreatType;
}
