package dk.patientassist.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.utilities.MockData;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
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
    static void setup() {
        try {
            jsonMapper = Utils.getObjectMapperCompact();
            jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
            jwtIss = Utils.getConfigProperty("JWT_ISSUER");
            jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
            jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
        } catch (Exception e) {
            Assertions.fail("setup failed");
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        port = 9999;
        jav = MasterController.start(Mode.TEST, port);

        empData = new EmployeeData();
        register(empData.guest, "guest");
        register(empData.admin, "admin");
    }

    @AfterAll
    static void teardown() {
        jav.stop();
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

    /* HELPER METHODS */
    static String get(String endpoint, int expStatus) {
        return RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwt)
                .when().get("/api/" + endpoint)
                .then().assertThat().statusCode(expStatus)
                .and().extract().body().asString();
    }

    static void register(EmployeeDTO empDetails, String pw) {
        try {
            jwt = RestAssured.given().port(port).contentType("application/json")
                    .body(empDetails.makeRegistrationForm(pw))
                    .when().post("/api/auth/register")
                    .then().statusCode(201)
                    .and().extract().path("token");
        } catch (Exception e) {
            Assertions.fail("registration error");
        }
    }

    static void login(EmployeeDTO empDetails, String pw) {
        try {
            jwt = RestAssured.given().port(port).contentType("application/json").body(empDetails.makeLoginForm(pw))
                    .when().post("/api/auth/login")
                    .then().statusCode(200)
                    .and().extract().path("token");
        } catch (Exception e) {
            Assertions.fail("login error");
        }
    }

    static String putEvent(Event event) {
        try {
            String empJson = jsonMapper.writeValueAsString(Mapper.EventEntToDTO(event));
            String res = RestAssured.given().port(port).contentType("application/json").body(empJson)
                    .header("Authorization", "Bearer " + jwt)
                    .when().put("/api/events")
                    .then().statusCode(201)
                    .and().extract().body().asString();
            return res;
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    static String patchEvent(int id, Event event) {
        try {
            String empJson = jsonMapper.writeValueAsString(Mapper.EventEntToDTO(event));
            String res = RestAssured.given().port(port).contentType("application/json").body(empJson)
                    .header("Authorization", "Bearer " + jwt)
                    .when().patch("/api/events/" + id)
                    .then().statusCode(200)
                    .and().extract().body().asString();
            return res;
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    static String deleteEvent(int id) {
        try {
            String res = RestAssured.given().port(port)
                    .header("Authorization", "Bearer " + jwt)
                    .when().delete("/api/events/" + id)
                    .then().statusCode(200)
                    .and().extract().body().asString();
            return res;
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    static void persistEvent(Event event) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            em.persist(event);
            em.getTransaction().commit();
        } catch (Exception e) {
            Assertions.fail("persisting event failed");
        }
    }

    static void eventCompare(Event a, Event b) {
        Assertions.assertNotNull(a, "event in test should not be null null");
        Assertions.assertEquals(b.name, a.name,
                "event retrieved equals event stored (name)");
        Assertions.assertEquals(b.description, a.description,
                "event retrieved equals event stored (description)");
        Assertions.assertEquals(b.startTime.withNano(0), a.startTime.withNano(0),
                "event retrieved equals event stored (startTime)");
        Assertions.assertEquals(b.duration.withNanos(0), a.duration.withNanos(0),
                "event retrieved equals event stored (duration)");
    }

    static void logout() {
        jwt = "";
    }
}
