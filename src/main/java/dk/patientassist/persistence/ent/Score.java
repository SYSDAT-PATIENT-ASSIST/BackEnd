package dk.patientassist.persistence.ent;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Patient Assist
 */
@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public Integer value;
    public String name;
    @Column(name = "time_of")
    public LocalDateTime timeOf;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "game_score", joinColumns = @JoinColumn(name = "game_id"), inverseJoinColumns = @JoinColumn(name = "score_id"))
    public Set<Score> scores;
}
