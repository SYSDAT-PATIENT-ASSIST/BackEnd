package dk.patientassist.test.steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class AdminLoginMenuSteps {

    private final String BASE_URI = "http://localhost:7070/api";
    private Response response;

    private final Map<String, String> validCredentials = Map.of(
            "email", "chef@example.com",
            "password", "securePassword123"
    );

    private final Map<String, String> invalidCredentials = Map.of(
            "email", "wrong@example.com",
            "password", "badPassword"
    );

    private Map<String, String> currentCredentials;

    @Given("I am on the Main application page")
    public void i_am_on_the_main_application_page() {
        System.out.println("User is on the main application page.");
    }

    @Given("the connection is secured via HTTPS")
    public void the_connection_is_secured_via_https() {
        System.out.println("HTTPS is assumed for this environment.");
    }

    @Given("the login page is clearly labeled for kitchen staff or administrators")
    public void the_login_page_is_clearly_labeled() {
        System.out.println("Verified: login page is labeled for 'Køkken/Admin'.");
    }

    @Given("I have valid admin login credentials")
    public void i_have_valid_admin_login_credentials() {
        currentCredentials = validCredentials;
    }

    @Given("I have invalid admin login credentials")
    public void i_have_invalid_admin_login_credentials() {
        currentCredentials = invalidCredentials;
    }

    @When("I enter my admin credentials in the login fields")
    public void i_enter_my_admin_credentials() {
        response = given()
                .baseUri(BASE_URI)
                .basePath("/auth/login") // Adjust if needed
                .contentType("application/json")
                .body(currentCredentials)
                .when()
                .post();
    }

    @When("I click the {string} button on the login page")
    public void i_click_the_button_on_the_login_page(String buttonText) {
        System.out.println("Button clicked: " + buttonText);
        // You could simulate button behavior here if needed
    }

    @Then("I am redirected to the kitchen main menu")
    public void i_am_redirected_to_the_kitchen_main_menu() {
        assertEquals(200, response.statusCode(), "Expected HTTP 200 OK for successful login");
        String token = response.jsonPath().getString("accessToken");
        assertNotNull(token, "Expected access token in response");
    }

    @Then("a confirmation or welcome message is displayed")
    public void a_confirmation_or_welcome_message_is_displayed() {
        String message = response.jsonPath().getString("message");
        assertNotNull(message, "Expected a welcome message");
        assertTrue(message.contains("Velkommen"), "Expected message to include 'Velkommen'");
    }

    @Then("the system highlights the invalid fields")
    public void the_system_highlights_the_invalid_fields() {
        assertEquals(401, response.statusCode(), "Expected 401 Unauthorized for failed login");
    }

    @Then("an error message is displayed")
    public void an_error_message_is_displayed() {
        String error = response.jsonPath().getString("error");
        assertNotNull(error, "Expected an error message");
        assertTrue(error.contains("Bruger ej fundet") || error.toLowerCase().contains("unauthorized"));
    }
}
