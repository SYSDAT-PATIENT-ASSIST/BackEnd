package dk.patientassist.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import dk.patientassist.api.resources.EmployeeData;
import dk.patientassist.api.resources.EventData;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.persistence.ent.ExamTreat;
import dk.patientassist.persistence.ent.Section;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.service.dto.ExamTreatDTO;
import dk.patientassist.service.dto.ExamTreatTypeDTO;
import dk.patientassist.utilities.ExamTreatPopulator;
import dk.patientassist.utilities.MockData;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * HelperMethods
 */
public class HelperMethods {

    static EntityManagerFactory emf;
    static Javalin jav;
    static ObjectMapper jsonMapper;
    public static String jwt;
    static EmployeeData empData;
    static EventData eventData;
    static int port;

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    static Faker faker = new Faker();
    static Random rng = new Random();

    public static void stop() {
        jav.stop();
    }

    public static void setup() {
        try {
            jsonMapper = Utils.getObjectMapperCompact();
            jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
            jwtIss = Utils.getConfigProperty("JWT_ISSUER");
            jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
            jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
        } catch (Exception e) {
            Assertions.fail("setup failed: " + e.getMessage());
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        port = 9999;
        jav = MasterController.start(Mode.TEST, port);

        empData = new EmployeeData();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Section sect0 = new Section();
            sect0.name = "zero";
            em.persist(sect0);
            Section sect1 = new Section();
            sect1.name = "one";
            em.persist(sect1);
            Section sect2 = new Section();
            sect2.name = "two";
            em.persist(sect2);
            em.getTransaction().commit();
        } catch (Exception e) {
            Assertions.fail("setup failed");
        }

        try {
            ExamTreatPopulator.load("data/exams_and_treatments_data.json");
        } catch (Exception e) {
            Assertions.fail("setup failed: " + e.getMessage());
        }
    }

    public static String get(String endpoint, int expStatus) {
        return RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwt)
                .when().get("/api/" + endpoint)
                .then().assertThat().statusCode(expStatus)
                .and().extract().body().asString();
    }

    public static ExamTreatCategoryDTO[] fetchExamTreatCategories() {
        String response = get("examinations-and-treatments/categories", 200);
        try {
            return Utils.getObjectMapperCompact().readValue(response, ExamTreatCategoryDTO[].class);
        } catch (JsonProcessingException e) {
            Assertions.fail("invalid JSON in server response");
            return null;
        }
    }

    public static ExamTreatTypeDTO[] fetchExamTreatSubcategories(String category) {
        String response = get("examinations-and-treatments/categories/" + category, 200);
        try {
            return Utils.getObjectMapperCompact().readValue(response, ExamTreatTypeDTO[].class);
        } catch (JsonProcessingException e) {
            Assertions.fail("invalid JSON in server response");
            return null;
        }
    }

    public static ExamTreatDTO fetchExamTreatArticles(String subcategory) {
        String response = get("examinations-and-treatments/articles/" + subcategory, 200);
        try {
            return Utils.getObjectMapperCompact().readValue(response, ExamTreatDTO.class);
        } catch (JsonProcessingException e) {
            Assertions.fail("invalid JSON in server response");
            return null;
        }
    }

    public static void matchCategories(ExamTreatCategoryDTO[] a, ExamTreatCategoryDTO[] b) {
        Assertions.assertEquals(a.length, b.length,
                "ET data should have same size on disk and in API");
        for (var ETCat : a) {
            boolean found = false;
            for (var ETCatInResponse : b) {
                if (ETCat.name.equals(ETCatInResponse.name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assertions.fail("Examinations and treatment data on disk does not match api response");
            }
        }
    }

    public static void matchCategories(ExamTreatCategoryDTO a, ExamTreatCategoryDTO b) {
        Assertions.assertEquals(a.name, b.name);
        Assertions.assertEquals(a.urlSafeName, b.urlSafeName);
    }

    public static void matchSubCategories(ExamTreatTypeDTO[] a, ExamTreatTypeDTO[] b) {
        Assertions.assertEquals(a.length, b.length);
        Arrays.sort(a);
        Arrays.sort(b);
        for (int i = 0; i < a.length; i++) {
            Assertions.assertEquals(a[i].name, b[i].name);
            Assertions.assertEquals(a[i].urlSafeName, b[i].urlSafeName);
        }
    }

    public static void matchArticles(ExamTreatDTO a, ExamTreatDTO b) {
        Assertions.assertEquals(a.name, b.name);
        Assertions.assertEquals(a.urlSafeName, b.urlSafeName);
    }

    public static void register(EmployeeDTO empDetails, String pw) {
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

    public static void login(EmployeeDTO empDetails, String pw) {
        try {
            jwt = RestAssured.given().port(port).contentType("application/json").body(empDetails.makeLoginForm(pw))
                    .when().post("/api/auth/login")
                    .then().statusCode(200)
                    .and().extract().path("token");
        } catch (Exception e) {
            Assertions.fail("login error");
        }
    }

    public static void logout() {
        jwt = "";
    }

    public static EventDTO putEvent(Event event) {
        try {
            String empJson = jsonMapper.writeValueAsString(Mapper.EventEntToDTO(event));
            return RestAssured.given().port(port).contentType("application/json").body(empJson)
                    .header("Authorization", "Bearer " + jwt)
                    .when().put("/api/events")
                    .then().statusCode(201)
                    .and().extract().body().as(EventDTO.class);
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    public static EventDTO getEvent(int id) {
        try {
            return RestAssured.given().port(port)
                    .when().get("/api/events/" + id)
                    .then().statusCode(200)
                    .and().extract().body().as(EventDTO.class);
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    public static EventDTO patchEvent(int id, Event event) {
        try {
            String empJson = jsonMapper.writeValueAsString(Mapper.EventEntToDTO(event));
            return RestAssured.given().port(port).contentType("application/json").body(empJson)
                    .header("Authorization", "Bearer " + jwt)
                    .when().patch("/api/events/" + id)
                    .then().statusCode(200)
                    .and().extract().body().as(EventDTO.class);
        } catch (Exception e) {
            Assertions.fail("registration error");
            return null;
        }
    }

    public static String deleteEvent(int id) {
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

    public static void persistEvent(Event event) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            em.persist(event);
            em.getTransaction().commit();
        } catch (Exception e) {
            Assertions.fail("persisting event failed");
        }
    }

    public static boolean eventCompare(Event a, Event b) {
        Assertions.assertNotNull(a, "event in test should not be null null");
        Assertions.assertEquals(b.name, a.name,
                "event retrieved equals event stored (name)");
        Assertions.assertEquals(b.description, a.description,
                "event retrieved equals event stored (description)");
        Assertions.assertEquals(b.startTime.withNano(0), a.startTime.withNano(0),
                "event retrieved equals event stored (startTime)");
        Assertions.assertEquals(b.duration.withNanos(0), a.duration.withNanos(0),
                "event retrieved equals event stored (duration)");
        return true;
    }

    public static void wipeEvents() {
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
    }

    public static EventDTO[] getEvents() {
        try {
            return Utils.getObjectMapperCompact().readValue(get("events", 200), EventDTO[].class);
        } catch (Exception e) {
            Assertions.fail(String.format("reading events failed: %s", e.getMessage()));
        }
        return new EventDTO[0];
    }

}
