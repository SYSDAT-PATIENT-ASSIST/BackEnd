package dk.patientassist.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.patientassist.api.testresources.EmployeeData;
import dk.patientassist.api.testresources.EventData;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * TestEvent
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

    @BeforeAll
    static void setup() {
        try {
            jsonMapper = Utils.getObjectMapperCompact();
            jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
            jwtIss = Utils.getConfigProperty("JWT_ISSUER");
            jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
            jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
        } catch (Exception e) {
            fail("setup failed");
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        empData = new EmployeeData();
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            em.persist(empData.guest);
            em.persist(empData.admin);
            em.getTransaction().commit();
        } catch (Exception e) {
            fail("setup failed");
        }

        port = 9999;
        jav = MasterController.start(Mode.TEST, port);
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
            fail("setup failed");
        }
        logout();
    }

    /* TESTS */

    @Test
    void read() {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            em.persist(eventData.simpleEvent);
            em.getTransaction().commit();
        } catch (Exception e) {
            fail("setup failed");
        }

        EventDTO[] events = new EventDTO[0];
        try {
            String eventResponse = get("events", 200);
            events = Utils.getObjectMapperCompact().readValue(eventResponse, EventDTO[].class);
        } catch (Exception e) {
            fail(String.format("reading events failed: %s", e.getMessage()));
        }

        assertEquals("expected number of events", events.length, 1);
        assertEquals("event retrieved equals event stored (name)", eventData.simpleEvent.name, events[0].name);
        assertEquals("event retrieved equals event stored (description)", eventData.simpleEvent.description,
                events[0].description);
        assertEquals("event retrieved equals event stored (startTime)", eventData.simpleEvent.startTime,
                events[0].startTime);
        assertEquals("event retrieved equals event stored (duration)", eventData.simpleEvent.duration,
                events[0].duration);
    }

    @Test
    void create() {

    }

    @Test
    void update() {

    }

    @Test
    void delete() {

    }

    /* HELPER METHODS */

    static String get(String endpoint, int expStatus) {
        return RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwt)
                .when().get("/api/" + endpoint)
                .then().assertThat().statusCode(expStatus)
                .and().extract().body().asString();
    }

    static void login(EmployeeDTO empDetails, String pw) {
        try {
            String empJson = jsonMapper.writeValueAsString(empDetails);
            empJson = empJson.substring(0, empJson.indexOf('{') + 1)
                    + String.format("\"password\": \"%s\",", pw) + empJson.substring(empJson.indexOf('{') + 1);
            jwt = RestAssured.given().port(port).contentType("application/json").body(empJson)
                    .when().post("/api/auth/login")
                    .then().statusCode(200)
                    .and().extract().path("token");
        } catch (Exception e) {
            fail("login error");
        }
    }

    static void logout() {
        jwt = "";
    }
}
