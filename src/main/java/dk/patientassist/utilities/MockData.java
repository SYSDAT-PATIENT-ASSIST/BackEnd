package dk.patientassist.utilities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

import com.github.javafaker.Faker;

import dk.patientassist.persistence.ent.Event;

/**
 * MockData
 */
public class MockData {

    static Faker faker = new Faker();
    static Random rng = new Random();

    public static Event event() {
        Event event = new Event();
        event.id = null;
        event.name = faker.harryPotter().location();
        event.description = faker.harryPotter().quote();
        event.startTime = LocalDateTime.now().plusDays(rng.nextLong(-365, 365)).plusHours(rng.nextLong(-12, 12));
        event.duration = Duration.ofMinutes(rng.nextLong(1, 1000));
        return event;
    }
}
