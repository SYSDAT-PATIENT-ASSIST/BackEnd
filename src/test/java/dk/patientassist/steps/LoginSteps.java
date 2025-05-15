package dk.patientassist.steps;

import dk.patientassist.config.ApplicationConfig;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import io.cucumber.java.en.*;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import static org.junit.Assert.*;

public class LoginSteps {

    private static final String BASE_URI = "http://localhost:7070/api";

    private static Javalin app;
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    private static boolean serverStarted = false;

    private Response response;

    private String username;
    private String password;

    @Before(order = 0)
    public void startServerOnce() {
        if (!serverStarted) {
            HibernateConfig.Init(HibernateConfig.Mode.TEST);
            app = ApplicationConfig.startServer(7070);
            RestAssured.baseURI = BASE_URI;
            entityManagerFactory = HibernateConfig.getEntityManagerFactory();
            serverStarted = true;
        }
    }

    @Before(order = 1)
    public void openEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @After
    public void tearDownEach() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            entityManager.createQuery("DELETE FROM Role").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    // Gherkin step definitions

    @Given("there is a user with the username {string} and password {string}")
    public void thereIsAUser(String username, String password) {
        this.username = username;
        this.password = password;

        entityManager.getTransaction().begin();

        Role userRole = new Role("user");
        entityManager.persist(userRole);

        User user = new User(username, password);
        user.addRole(userRole);
        entityManager.persist(user);

        entityManager.getTransaction().commit();
    }

    @When("the user tries to log in with these details")
    public void userTriesToLoginWithTheseDetails() {
        response = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
                .post(BASE_URI + "/auth/login");
    }

    @When("the user tries to log in using the password {string}")
    public void userTriesToLoginWithPassword(String password) {
        response = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
                .post(BASE_URI + "/auth/login");
    }

    @When("someone tries to log in with the username {string} and any password")
    public void someoneTriesToLoginWithUsernameAndAnyPassword(String username) {
        response = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"" + username + "\", \"password\":\"any\"}")
                .post(BASE_URI + "/auth/login");
    }

    @When("someone tries to log in without entering a username or password")
    public void someoneTriesToLoginWithoutDetails() {
        response = RestAssured.given()
                .contentType("application/json")
                .body("{}")
                .post(BASE_URI + "/auth/login");
    }

    @Then("the system should respond with status {int} \\(OK\\)")
    public void systemShouldRespondWithStatusOK(int status) {
        assertEquals("Forventet statuskode OK", status, response.statusCode());
        // Sørg for at token også er med ved OK
        String token = response.jsonPath().getString("token");
        assertNotNull("Token skal ikke være null ved OK svar", token);
        assertFalse("Token må ikke være tom ved OK svar", token.isEmpty());
    }

    @Then("the system should respond with status {int} \\(Unauthorized\\)")
    public void systemShouldRespondWithStatusUnauthorized(int status) {
        assertEquals("Forventet statuskode Unauthorized", status, response.statusCode());
        // Tjek om der er fejlbesked med i responsen
        String msg = response.jsonPath().getString("msg");
        assertNotNull("Fejlmeddelelse skal være med ved Unauthorized", msg);
        assertFalse("Fejlmeddelelse må ikke være tom", msg.isEmpty());
    }

    @Then("the system should respond with status {int} \\(Bad Request\\)")
    public void systemShouldRespondWithStatusBadRequest(int status) {
        assertEquals("Forventet statuskode Bad Request", status, response.statusCode());
        // Tjek om fejlbesked for invalid input findes
        String error = response.jsonPath().getString("error");
        assertNotNull("Fejlbesked skal være med ved Bad Request", error);
        assertTrue("Fejlbesked skal indeholde 'must' eller 'invalid'",
                error.toLowerCase().contains("must") || error.toLowerCase().contains("invalid"));
    }


    @Then("the response should include a valid login token")
    public void responseShouldIncludeValidLoginToken() {
        String token = response.jsonPath().getString("token");
        assertNotNull("Token må ikke være null", token);
        assertFalse("Token må ikke være tom", token.isEmpty());
    }

    @Then("the response should include the username {string}")
    public void responseShouldIncludeTheUsername(String expectedUsername) {
        assertEquals("Brugernavn i svar er ikke som forventet", expectedUsername, response.jsonPath().getString("username"));
    }

    @Then("the response should contain the message {string}")
    public void responseShouldContainTheMessage(String message) {
        assertEquals("Besked i svar er ikke som forventet", message, response.jsonPath().getString("msg"));
    }

    @Then("the response should include an error message indicating invalid input")
    public void responseShouldIncludeErrorMessageInvalidInput() {
        String error = response.jsonPath().getString("error");
        assertNotNull("Fejlbesked skal være med", error);
        assertTrue("Fejlbesked skal indeholde 'invalid' eller 'must'",
                error.toLowerCase().contains("invalid") || error.toLowerCase().contains("must"));
    }

}
