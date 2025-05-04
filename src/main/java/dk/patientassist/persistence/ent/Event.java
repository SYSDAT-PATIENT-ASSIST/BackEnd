package dk.patientassist.persistence.ent;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Patient Assist
 */
@Entity
@EqualsAndHashCode
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    public String description;
    @Column(name = "start_time")
    public LocalDateTime startTime;
    @Column(name = "dur")
    public Duration duration;
}
