package dk.patientassist.persistence.ent;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "training_program_slide", joinColumns = @JoinColumn(name = "training_program_id"), inverseJoinColumns = @JoinColumn(name = "slide_id"))
    public Set<TrainingProgram> videos;
}
