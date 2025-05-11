package dk.patientassist.test.Stepdefinitions;

import dk.patientassist.persistence.HibernateConfig;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class SecurityStepDefinitions {

    private String baseUrl;
    private Response lastResponse;
    private String token;

    @Before
    public void beforeEachScenario() {
        // 1) point RestAssured at the API
        baseUrl = System.getProperty("baseUrl", "http://localhost:7070");
        RestAssured.baseURI = baseUrl;

        // 2) initialize Hibernate in TEST mode (Testcontainers + create-drop)
        HibernateConfig.Init(HibernateConfig.Mode.TEST);

        // 3) truncate all rows from User and Role
        var emf = HibernateConfig.getEntityManagerFactory();
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            // simple HQL entity names (default @Entity names)
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            tx.commit();
        }
    }

    @Given("the API is up on {string}")
    public void theApiIsUp(String url) {
        // allow feature to override host if desired
        RestAssured.baseURI = url;
        Response r = get("/auth/healthcheck");
        assertEquals(200, r.statusCode(), "Healthcheck should return 200");
    }

    @Given("I register with username {string} and password {string} as {string}")
    public void iRegister(String user, String pass, String role) {
        lastResponse = given()
                .queryParam("role", role)
                .contentType("application/json")
                .body("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}")
                .when()
                .post("/auth/register");
        assertEquals(201, lastResponse.statusCode(),
                "Expected 201 on register but got " + lastResponse.asString());
    }

    @When("I log in with username {string} and password {string}")
    public void iLogIn(String user, String pass) {
        lastResponse = given()
                .contentType("application/json")
                .body("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}")
                .when()
                .post("/auth/login");
    }

    @Then("the login status code is {int}")
    public void loginStatusCodeIs(int code) {
        assertEquals(code, lastResponse.statusCode(),
                "Login response body: " + lastResponse.asString());
    }

    @And("I save the returned token")
    public void iSaveTheReturnedToken() {
        token = lastResponse.jsonPath().getString("token");
        assertNotNull(token, "Token was null in login response");
    }

    @When("I access {string} with that token")
    public void iAccessWithToken(String endpoint) {
        lastResponse = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(endpoint);
    }

    @Then("the status code should be {int}")
    public void statusCodeShouldBe(int expected) {
        assertEquals(expected, lastResponse.statusCode(),
                "Access response body: " + lastResponse.asString());
    }
}
