package dk.patientassist.persistence.ent;

import java.time.LocalDateTime;

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
}
