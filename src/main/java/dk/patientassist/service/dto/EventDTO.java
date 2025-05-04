package dk.patientassist.service.dto;

import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Patient Assist
 */
@EqualsAndHashCode
public class EventDTO {
    public Integer id;
    public String name;
    public String description;
    public LocalDateTime startTime;
    public Duration duration;
}
