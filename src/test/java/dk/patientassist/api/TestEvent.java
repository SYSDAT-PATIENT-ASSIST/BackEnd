package dk.patientassist.api;

import static dk.patientassist.api.impl.HelperMethods.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import dk.patientassist.api.resources.EmployeeData;
import dk.patientassist.api.resources.EventData;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.utilities.MockData;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Event API tests
 */
public class TestEvent {

    static EntityManagerFactory emf;
    static Javalin jav;
    static ObjectMapper jsonMapper;
    static String jwt;
    static EmployeeData empData;
    static EventData eventData;
    static int port;

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    static Faker faker = new Faker();
    static Random rng = new Random();

    @BeforeAll
    static void init() {
        setup();
        empData = new EmployeeData();
        register(empData.guest, "guest");
        register(empData.admin, "admin");
    }

    @AfterAll
    static void teardown() {
        stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        eventData = new EventData();
        wipeEvents();
        logout();
    }

    /* TESTS */

    @Test
    void read() {
        persistEvent(eventData.simpleEvent);
        var events = getEvents();
        Assertions.assertEquals(events.length, 1, "expected number of events differ");
        Assertions.assertEquals(eventCompare(eventData.simpleEvent, Mapper.EventDTOToEnt(events[0])), true,
                "expected same event in DB & from API");
    }

    @Test
    void create() {
        login(empData.admin, "admin");

        for (int i = 0; i < 10; i++) {
            Event event = MockData.event();
            var resDTO = putEvent(event);

            eventCompare(event, Mapper.EventDTOToEnt(resDTO));

            var resDTORetrieved = getEvent(resDTO.id);

            eventCompare(Mapper.EventDTOToEnt(resDTORetrieved), Mapper.EventDTOToEnt(resDTO));
        }
    }

    @Test
    void update() {
        login(empData.admin, "admin");
        Event event = MockData.event();

        var eventPutDTO = putEvent(event);
        Assertions.assertEquals(eventCompare(event, Mapper.EventDTOToEnt(eventPutDTO)), true,
                "expected same event in DB & from API");

        event.id = eventPutDTO.id;
        event.name = event.name + event.name;
        event.description = event.description + event.description;
        event.startTime = event.startTime.plusSeconds(rng.nextLong(-100000, 100000));
        event.duration = event.duration.plusSeconds(rng.nextLong(-100000, 100000));

        var eventPatchedDTO = patchEvent(event.id, event);
        Assertions.assertEquals(eventCompare(event, Mapper.EventDTOToEnt(eventPatchedDTO)), true,
                "expected same event in DB & from API");

        eventPatchedDTO = getEvent(event.id);
        Assertions.assertEquals(eventCompare(event, Mapper.EventDTOToEnt(eventPatchedDTO)), true,
                "expected same event in DB & from API");
    }

    @Test
    void delete() {
        login(empData.admin, "admin");

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = MockData.event();
            var eventDTO = putEvent(event);
            event.id = eventDTO.id;
            events.add(event);
        }

        for (int i = 0; i < 10; i++) {
            int removeIdx = rng.nextInt(0, events.size());
            Event event = events.get(removeIdx);
            events.remove(removeIdx);

            var eventsFetched = getEvents();

            Assertions.assertEquals(10 - i, eventsFetched.length, "fetched events size should match");

            deleteEvent(event.id);

            eventsFetched = getEvents();

            Assertions.assertEquals(10 - i - 1, eventsFetched.length, "fetched events size should match");

            for (var dto : eventsFetched) {
                if (dto.id == event.id) {
                    Assertions.fail("fetched deleted event");
                }
            }
        }
    }
}
