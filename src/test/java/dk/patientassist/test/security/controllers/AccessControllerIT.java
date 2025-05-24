package dk.patientassist.test.security.controllers;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.HibernateConfig.Mode;
import dk.patientassist.security.controllers.AccessController;
import dk.patientassist.security.enums.Role;
import dk.patientassist.security.routes.SecurityRoutes;
import io.javalin.Javalin;
//import io.javalin.community.routing.RoutingPlugin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AccessController as Javalin's AccessManager.
 * Uses real Postgres (via Testcontainers) and real JWT.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccessControllerIT {

   /* private Javalin app;

    @BeforeAll
    void setup() {
        // 1) Start Hibernate + Testcontainers Postgres
        HibernateConfig.Init(Mode.TEST);

        // 2) Build Javalin with our AccessController and routes
        app = Javalin.create(cfg -> {
            cfg.plugins.register(new RoutingPlugin());
            cfg.accessManager(new AccessController()::accessHandler);
            // Mount the real /auth and /protected routes
            cfg.routes(SecurityRoutes.getSecurityRoutes());
            cfg.routes(SecurityRoutes.getSecuredRoutes());
            // Add test‐only endpoints under /test
            cfg.routes(() -> {
                get("/test/any", ctx -> ctx.result("any"), Role.ANYONE);
                get("/test/admin", ctx -> ctx.result("admin"), Role.ADMIN);
                get("/test/kok_hoved", ctx -> ctx.result("kok_hoved"), Role.KOK, Role.HOVEDKOK);
            });
        }).start(0);

        // 3) Point RestAssured at our Javalin instance
        RestAssured.baseURI = "http://localhost";
        RestAssured.port    = app.port();
    }

    @AfterAll
    void teardown() {
        app.stop();
    }

    @Test
    void publicEndpoint_allowedWithoutAuth() {
        when()
                .get("/test/any")
                .then()
                .statusCode(200)
                .body(equalTo("any"));
    }

    @Test
    void adminEndpoint_failsWithoutToken() {
        when()
                .get("/test/admin")
                .then()
                .statusCode(401);
    }

    @Test
    void adminEndpoint_failsWithWrongRole() {
        // Register as KOK
        String kokToken =
                given()
                        .queryParam("role", "KOK")
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"joe\",\"password\":\"pw\"}")
                        .when()
                        .post("/auth/register")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + kokToken)
                .when()
                .get("/test/admin")
                .then()
                .statusCode(401);
    }

    @Test
    void adminEndpoint_succeedsWithAdminRole() {
        // Register as ADMIN
        String adminToken =
                given()
                        .queryParam("role", "ADMIN")
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"alice\",\"password\":\"pw\"}")
                        .when()
                        .post("/auth/register")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/test/admin")
                .then()
                .statusCode(200)
                .body(equalTo("admin"));
    }

    @Test
    void oneOfRoles_kokAllowed() {
        // Register as KOK
        String kokToken =
                given()
                        .queryParam("role", "KOK")
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"mike\",\"password\":\"pw\"}")
                        .when()
                        .post("/auth/register")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + kokToken)
                .when()
                .get("/test/kok_hoved")
                .then()
                .statusCode(200)
                .body(equalTo("kok_hoved"));
    }

    @Test
    void oneOfRoles_hovedkokAllowed() {
        // Register as HOVEDKOK
        String hovToken =
                given()
                        .queryParam("role", "HOVEDKOK")
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"eva\",\"password\":\"pw\"}")
                        .when()
                        .post("/auth/register")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + hovToken)
                .when()
                .get("/test/kok_hoved")
                .then()
                .statusCode(200)
                .body(equalTo("kok_hoved"));
    }

    @Test
    void oneOfRoles_adminNotAllowed() {
        // Register as ADMIN
        String adminToken =
                given()
                        .queryParam("role", "ADMIN")
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"sam\",\"password\":\"pw\"}")
                        .when()
                        .post("/auth/register")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/test/kok_hoved")
                .then()
                .statusCode(401);
    }*/
}
