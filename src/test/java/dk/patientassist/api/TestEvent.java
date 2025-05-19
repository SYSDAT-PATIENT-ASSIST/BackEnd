package dk.patientassist.api;

import static dk.patientassist.api.impl.HelperMethods.deleteEvent;
import static dk.patientassist.api.impl.HelperMethods.eventCompare;
import static dk.patientassist.api.impl.HelperMethods.get;
import static dk.patientassist.api.impl.HelperMethods.login;
import static dk.patientassist.api.impl.HelperMethods.logout;
import static dk.patientassist.api.impl.HelperMethods.patchEvent;
import static dk.patientassist.api.impl.HelperMethods.persistEvent;
import static dk.patientassist.api.impl.HelperMethods.putEvent;
import static dk.patientassist.api.impl.HelperMethods.register;
import static dk.patientassist.api.impl.HelperMethods.setup;
import static dk.patientassist.api.impl.HelperMethods.stop;

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
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            List<Event> events = em.createQuery("SELECT e from Event e", Event.class).getResultList();
            for (Event e : events) {
                em.remove(e);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            Assertions.fail("setup failed");
        }
        logout();
    }

    /* TESTS */

    @Test
    void read() {
        persistEvent(eventData.simpleEvent);

        EventDTO[] events = new EventDTO[0];
        String eventResponse = get("events", 200);
        try {
            events = Utils.getObjectMapperCompact().readValue(eventResponse, EventDTO[].class);
        } catch (Exception e) {
            Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
        }

        Assertions.assertEquals(events.length, 1, "expected number of events");
        eventCompare(eventData.simpleEvent, Mapper.EventDTOToEnt(events[0]));
    }

    @Test
    void create() {
        login(empData.admin, "admin");

        EventDTO resDTO = null, resDTORetrieved = null;
        String responseStr = "";

        for (int i = 0; i < 10; i++) {
            Event event = MockData.event();

            responseStr = putEvent(event);
            try {
                resDTO = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO.class);
            } catch (Exception e) {
                Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
            }

            eventCompare(event, Mapper.EventDTOToEnt(resDTO));

            responseStr = get("events/" + resDTO.id, 200);

            try {
                resDTORetrieved = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO.class);
            } catch (Exception e) {
                Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
            }

            eventCompare(Mapper.EventDTOToEnt(resDTORetrieved), Mapper.EventDTOToEnt(resDTO));
        }

        responseStr = putEvent(eventData.simpleEvent);

        try {
            resDTO = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO.class);
        } catch (Exception e) {
            Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
        }

        eventCompare(eventData.simpleEvent, Mapper.EventDTOToEnt(resDTO));
    }

    @Test
    void update() {
        login(empData.admin, "admin");
        Event event = MockData.event();

        String eventPutStr = putEvent(event);
        EventDTO eventPutDTO = null;
        try {
            eventPutDTO = Utils.getObjectMapperCompact().readValue(eventPutStr, EventDTO.class);
        } catch (Exception e) {
            Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
        }

        eventCompare(event, Mapper.EventDTOToEnt(eventPutDTO));

        event.id = eventPutDTO.id;
        event.name = event.name + event.name;
        event.description = event.description + event.description;
        event.startTime = event.startTime.plusSeconds(rng.nextLong(-100000, 100000));
        event.duration = event.duration.plusSeconds(rng.nextLong(-100000, 100000));

        patchEvent(event.id, event);
        String eventPatchedStr = get("events/" + event.id, 200);
        EventDTO eventPatchedDTO = null;
        try {
            eventPatchedDTO = Utils.getObjectMapperCompact().readValue(eventPatchedStr, EventDTO.class);
        } catch (Exception e) {
            Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
        }

        eventCompare(event, Mapper.EventDTOToEnt(eventPatchedDTO));
    }

    @Test
    void delete() {
        login(empData.admin, "admin");

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = MockData.event();
            String responseStr = putEvent(event);
            try {
                EventDTO eventDTO = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO.class);
                event.id = eventDTO.id;
            } catch (Exception e) {
                Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
            }
            events.add(event);
        }

        for (int i = 0; i < 10; i++) {
            int removeIdx = rng.nextInt(0, events.size());
            Event event = events.get(removeIdx);
            events.remove(removeIdx);

            EventDTO[] eventsFetched = new EventDTO[0];
            String responseStr = get("events", 200);
            try {
                eventsFetched = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO[].class);
            } catch (Exception e) {
                Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
            }

            Assertions.assertEquals(10 - i, eventsFetched.length, "fetched events size should match");

            deleteEvent(event.id);

            responseStr = get("events", 200);
            try {
                eventsFetched = Utils.getObjectMapperCompact().readValue(responseStr, EventDTO[].class);
            } catch (Exception e) {
                Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
            }

            Assertions.assertEquals(10 - i - 1, eventsFetched.length, "fetched events size should match");
            for (var dto : eventsFetched) {
                if (dto.id == event.id) {
                    Assertions.fail("fetched deleted event");
                }
            }
        }
    }
}
