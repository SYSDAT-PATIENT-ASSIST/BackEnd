package dk.patientassist;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;

/**
 * Patient Assist
 */

public class TestAuth {

    static Javalin jav;
    static EntityManagerFactory emf;
    static ObjectMapper jsonMapper;
    static int port;
    static String jwtAdmin = "";

    @BeforeAll
    static void setup() {
        try {
            jsonMapper = Utils.getObjectMapperCompact();
        } catch (Exception e) {
            assert (false);
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        port = 9999;
        jav = MasterController.start(port);
    }

    @AfterAll
    static void teardown() {
        jav.stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        logout();
    }

    /* TEST METHODS */

    @Test
    void testRegisterAdmin() {
        String json = """
                {
                    "email": "admin@example.com",
                    "password": "admin",
                    "first_name": "John",
                    "middle_name": "Doe",
                    "last_name": "Smith",
                    "roles": ["admin"],
                    "sections": [1, 2, 5]
                }
                """;

        RestAssured.given().port(port)
                .body(json)
                .when().post("/api/auth/register")
                .then().assertThat().statusCode(201);
        loginAsAdmin();
        RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwtAdmin)
                .when().get("/api/auth/admin_only")
                .then().assertThat().statusCode(200);
    }

    @Test
    void testLoginAdmin() {
        RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwtAdmin)
                .when().get("/api/auth/admin_only")
                .then().assertThat().statusCode(403);
        loginAsAdmin();
        RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwtAdmin)
                .when().get("/api/auth/admin_only")
                .then().assertThat().statusCode(200);
    }

    /* HELPER METHODS */

    static void loginAsAdmin() {
        String json = jsonMapper.createObjectNode()
                .put("email", "admin@example.com").put("password", "admin").toString();
        jwtAdmin = RestAssured.given().port(port).contentType("application/json").body(json)
                .when().post("/api/auth/login")
                .then().extract().path("token");
    }

    static void logout() {
        jwtAdmin = "";
    }
}
