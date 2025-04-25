package dk.patientassist.persistence.ent;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class TrainingProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @ManyToMany
    @JoinTable(name = "training_program_slide", joinColumns = @JoinColumn(name = "slide_id"), inverseJoinColumns = @JoinColumn(name = "training_program_id"))
    public Set<Slide> slides;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "training_program_video", joinColumns = @JoinColumn(name = "video_id"), inverseJoinColumns = @JoinColumn(name = "training_program_id"))
    public Set<Video> videos;
}
