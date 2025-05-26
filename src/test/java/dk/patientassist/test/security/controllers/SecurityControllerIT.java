package dk.patientassist.test.security.controllers;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.security.routes.SecurityRoutes;
import io.javalin.Javalin;
//import io.javalin.community.routing.RoutingPlugin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Full‐stack integration tests for SecurityController routes,
 * backed by a real Postgres (Testcontainers) and real JWT.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityControllerIT {

   /*
    * private Javalin app;
    * 
    * @BeforeAll
    * void setup() {
    * // 1) Start Hibernate + Testcontainers Postgres in TEST mode
    * HibernateConfig.Init(Mode.TEST);
    * 
    * // 2) Build Javalin with the RoutingPlugin and register routes
    * app = Javalin.create(cfg -> {
    * cfg.plugins.register(new RoutingPlugin());
    * cfg.routes(SecurityRoutes.getSecurityRoutes());
    * cfg.routes(SecurityRoutes.getSecuredRoutes());
    * }).start(0);
    * 
    * // 3) Configure RestAssured to point at this Javalin instance
    * RestAssured.baseURI = "http://localhost";
    * RestAssured.port = app.port();
    * }
    * 
    * @AfterAll
    * void teardown() {
    * app.stop();
    * }
    * 
    * @Test
    * void registerLoginAndAccessProtected() {
    * // Register a new ADMIN user
    * String token =
    * given()
    * .queryParam("role", "ADMIN")
    * .contentType(ContentType.JSON)
    * .body("{\"username\":\"it_alice\",\"password\":\"pass123\"}")
    * .when()
    * .post("/auth/register")
    * .then()
    * .statusCode(201)
    * .body("username", equalTo("it_alice"))
    * .body("token", not(emptyString()))
    * .extract()
    * .path("token");
    * 
    * // Login with the same credentials
    * given()
    * .contentType(ContentType.JSON)
    * .body("{\"username\":\"it_alice\",\"password\":\"pass123\"}")
    * .when()
    * .post("/auth/login")
    * .then()
    * .statusCode(200)
    * .body("username", equalTo("it_alice"))
    * .body("token", not(emptyString()));
    * 
    * // Access the ADMIN-protected endpoint
    * given()
    * .header("Authorization", "Bearer " + token)
    * .when()
    * .get("/protected/admin_demo")
    * .then()
    * .statusCode(200)
    * .body("msg", equalTo("Hello from ADMIN Protected"));
    * 
    * // Access the USER-demo endpoint (also protected by ADMIN)
    * given()
    * .header("Authorization", "Bearer " + token)
    * .when()
    * .get("/protected/user_demo")
    * .then()
    * .statusCode(200)
    * .body("msg", equalTo("Hello from USER Protected"));
    * }
    * 
    * @Test
    * void accessWithoutTokenFails() {
    * when()
    * .get("/protected/admin_demo")
    * .then()
    * .statusCode(401);
    * }
    */
}
