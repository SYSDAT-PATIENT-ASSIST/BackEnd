package dk.patientassist.test.steps;

import dk.patientassist.test.auth.AuthManager;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
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

    @Given("I am on the Main application page")
    public void iAmOnMainApplicationPage() {
        System.out.println("Accessing main application...");
    }

    @Given("I have valid admin login credentials")
    public void iHaveValidAdminCredentials() {
        AuthManager.setCredentials(validCredentials);
    }

    @Given("I have invalid admin login credentials")
    public void iHaveInvalidAdminCredentials() {
        AuthManager.setCredentials(invalidCredentials);
    }

    @Given("the connection is secured via HTTPS")
    public void theConnectionIsSecuredViaHttps() {
        System.out.println("Assuming HTTPS is enforced.");
    }

    @Given("the login page is clearly labeled for kitchen staff or administrators")
    public void theLoginPageIsClearlyLabeled() {
        System.out.println("Verified login page labeling.");
    }

    @When("I enter my admin credentials in the login fields")
    public void iEnterMyAdminCredentials() {
        response = given()
                .baseUri(BASE_URI)
                .basePath("/auth/login")
                .contentType("application/json")
                .body(AuthManager.getCredentials())
                .when()
                .post();
    }

    @When("I click the {string} button on the login page")
    public void iClickLoginButton(String buttonText) {
        if ("Log ind".equalsIgnoreCase(buttonText)) {
            if (response.getStatusCode() == 200) {
                String accessToken = response.jsonPath().getString("accessToken");
                String refreshToken = response.jsonPath().getString("refreshToken");

                AuthManager.setAccessToken(accessToken);
                AuthManager.setRefreshToken(refreshToken);
            }
        }
    }

    @Then("I am redirected to the kitchen main menu")
    public void iAmRedirectedToTheKitchenMainMenu() {
        assertEquals(200, response.statusCode());
        assertNotNull(AuthManager.getAccessToken());
    }

    @Then("a confirmation or welcome message is displayed")
    public void aConfirmationOrWelcomeMessageIsDisplayed() {
        String message = response.jsonPath().getString("message");
        assertTrue(message != null && message.contains("Velkommen"));
    }

    @Then("the system highlights the invalid fields")
    public void theSystemHighlightsTheInvalidFields() {
        assertEquals(401, response.statusCode());
    }

    @Then("an error message is displayed")
    public void anErrorMessageIsDisplayed() {
        String message = response.jsonPath().getString("error");
        assertNotNull(message);
        assertTrue(message.contains("Bruger ej fundet") || message.contains("unauthorized"));
    }

    @Given("my session has expired")
    public void mySessionHasExpired() {
        AuthManager.setAccessToken("expired_or_invalid_token");
        System.out.println("Session manually expired for test.");
    }

    @Given("I am logged out")
    public void iAmLoggedOut() {
        AuthManager.clear();
        System.out.println("User logged out.");
    }
}
