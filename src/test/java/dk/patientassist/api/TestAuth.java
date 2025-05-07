package dk.patientassist.api;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.patientassist.api.resources.EmployeeData;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.ent.Section;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Authentication tests
 */
public class TestAuth {

    static EntityManagerFactory emf;
    static Javalin jav;
    static ObjectMapper jsonMapper;
    static String jwt;
    static EmployeeData testData;
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
            Assertions.fail("setup failed");
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        port = 9999;
        jav = MasterController.start(Mode.TEST, port);

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
    }

    @AfterAll
    static void teardown() {
        jav.stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        testData = new EmployeeData();
        logout();
    }

    /* TESTS */

    @Test
    void testRegistration() {
        try {
            register(testData.guest, "guest");
            login(testData.guest, "guest");
            get("auth/guest", 200);
            register(testData.admin, "admin");
            login(testData.admin, "admin");
            get("auth/admin_only", 200);
            register(testData.chef, "chef");
            login(testData.chef, "chef");
            get("auth/chef_only", 200);
            register(testData.headchef, "headchef");
            login(testData.headchef, "headchef");
            get("auth/headchef_only", 200);
            register(testData.nurse, "nurse");
            login(testData.nurse, "nurse");
            get("auth/nurse_only", 200);
            register(testData.doctor, "doctor");
            login(testData.doctor, "doctor");
            get("auth/doctor_only", 200);
        } catch (Exception e) {
            Assertions.fail("registration error");
        }
    }

    @Test
    void testAccessDenied() {
        get("auth/guest", 200);
        get("auth/admin_only", 403);
        get("auth/chef_only", 403);
        get("auth/headchef_only", 403);
        get("auth/doctor_only", 403);
        get("auth/nurse_only", 403);
    }

    @Test
    void testSectionsAndRoles() { // maybe randomize and hammer these types of tests
        Long[] sections = new Long[] { 1L, 2L, 3L }; // these have to actually exist, consider randomly creating and
        // then fetching at random from db
        Role[] roles = new Role[] { Role.DOCTOR, Role.ADMIN, Role.GUEST };

        EmployeeDTO emp = new EmployeeDTO();
        emp.email = "test@example.com";
        emp.firstName = "testName";
        emp.lastName = "testName";
        emp.sections = sections;
        emp.roles = roles;
        emp.setPassword("test");

        register(emp, "test");
        login(emp, "test");
        get("auth/doctor_only", 200);

        DecodedJWT jwtDec = JWT.decode(jwt);
        Long[] sectionsInResp = jwtDec.getClaim("sectionIds").asArray(Long.class);
        Role[] rolesInResp = jwtDec.getClaim("roles").asArray(Role.class);

        Arrays.sort(sections);
        Arrays.sort(sectionsInResp);
        Arrays.sort(roles);
        Arrays.sort(rolesInResp);

        Assertions.assertArrayEquals(sections, sectionsInResp);
        Assertions.assertArrayEquals(roles, rolesInResp);
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

    static void logout() {
        jwt = "";
    }
}
