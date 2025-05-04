package dk.patientassist.api.testresources;

import com.github.javafaker.Faker;
import com.github.javafaker.HarryPotter;
import dk.patientassist.persistence.ent.Event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * EventData
 */
public class EventData {

    static Random rng = new Random(System.currentTimeMillis());
    static Faker fakeGenerator = new Faker();
    public Event simpleEvent;

    public EventData() {
        simpleEvent = new Event();
        simpleEvent.name = "simple event";
        simpleEvent.description = "just a simple event";
        simpleEvent.startTime = LocalDateTime.of(2025, 5, 4, 12, 30, 0, 0);
        simpleEvent.duration = Duration.ofMinutes(60);
    }

    public static Event getRandomEvent() {
        Event event = new Event();
        HarryPotter hp = fakeGenerator.harryPotter();
        event.name = hp.spell();
        event.description = hp.quote();
        event.startTime = LocalDateTime.now().plusDays(rng.nextLong(-365, 365)).plusHours(rng.nextLong(-24, 24));
        event.duration = Duration.ofMinutes(rng.nextLong(30, 300));
        return event;
    }
}
