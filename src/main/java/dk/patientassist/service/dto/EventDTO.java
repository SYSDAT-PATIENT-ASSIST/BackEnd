package dk.patientassist.service.dto;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;

/**
 * Patient Assist
 */
@EqualsAndHashCode
public class EventDTO {
    public Integer id;
    public String name;
    public String description;
    public LocalDateTime startTime;
    public Duration dur;
}
