package dk.patientassist.persistence.ent;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Patient Assist
 */
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @Column(name = "start_time")
    public LocalDateTime startTime;
    @Column(name = "end_time")
    public LocalDateTime endTime;
}
