package dk.patientassist.persistence.ent;

import java.time.Duration;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    public String description;
    public Duration length;
    public byte[] data;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "training_program_video", joinColumns = @JoinColumn(name = "training_program_id"), inverseJoinColumns = @JoinColumn(name = "video_id"))
    public Set<TrainingProgram> videos;
}
