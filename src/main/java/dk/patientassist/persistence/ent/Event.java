package dk.patientassist.persistence.ent;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Patient Assist
 */
@Entity
public class Event
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    @Column(name = "start_time")
    public LocalDateTime startTime;
    @Column(name = "end_time")
    public LocalDateTime endTime;
}
