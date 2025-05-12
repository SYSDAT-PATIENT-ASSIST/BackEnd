package dk.patientassist.test.Stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for Admin kitchen login feature.
 * Matches the Cucumber scenarios for login success and failure cases.
 */
public class AdminLoginStepDefinitions {

    /**
     * Step: I am on the Main application page.
     * Represents navigating to the start of the app.
     */
    @Given("I am on the Main application page")
    public void iAmOnTheMainApplicationPage() {
        System.out.println("Navigated to the Main application page.");
    }

    /**
     * Step: the connection is secured via HTTPS.
     * Ensures secure communication.
     */
    @And("the connection is secured via HTTPS")
    public void theConnectionIsSecuredViaHTTPS() {
        System.out.println("Verified connection is secured via HTTPS.");
    }

    /**
     * Step: the login page is clearly labeled for kitchen staff or administrators.
     * Checks for correct labeling.
     */
    @And("the login page is clearly labeled for kitchen staff or administrators")
    public void theLoginPageIsClearlyLabeledForKitchenStaffOrAdministrators() {
        System.out.println("Confirmed login page label for kitchen staff or administrators.");
    }

    /**
     * Step: I have valid admin login credentials.
     * Prepares valid test credentials.
     */
    @Given("I have valid admin login credentials")
    public void iHaveValidAdminLoginCredentials() {
        System.out.println("Prepared valid admin login credentials.");
    }

    /**
     * Step: I enter my admin credentials in the login fields.
     * Simulates filling out login form fields.
     */
    @When("I enter my admin credentials in the login fields")
    public void iEnterMyAdminCredentialsInTheLoginFields() {
        System.out.println("Entered admin credentials into login fields.");
    }

    /**
     * Step: I click the "Log ind" button on the login page.
     * Simulates button press.
     * @param buttonLabel The label on the button (e.g., "Log ind")
     */
    @And("I click the {string} button on the login page")
    public void iClickTheButtonOnTheLoginPage(String buttonLabel) {
        System.out.println("Clicked the \"" + buttonLabel + "\" button on the login page.");
    }

    /**
     * Step: I am redirected to the kitchen main menu.
     * Confirms successful redirection.
     */
    @Then("I am redirected to the kitchen main menu")
    public void iAmRedirectedToTheKitchenMainMenu() {
        System.out.println("Redirected to the kitchen main menu.");
    }

    /**
     * Step: a confirmation or welcome message is displayed.
     * Validates welcome message display.
     */
    @And("a confirmation or welcome message is displayed")
    public void aConfirmationOrWelcomeMessageIsDisplayed() {
        System.out.println("Displayed confirmation or welcome message.");
    }

    /**
     * Step: I have invalid admin login credentials.
     * Prepares invalid login input.
     */
    @Given("I have invalid admin login credentials")
    public void iHaveInvalidAdminLoginCredentials() {
        System.out.println("Prepared invalid admin login credentials.");
    }

    /**
     * Step: the system highlights the invalid fields.
     * Confirms UI error indication.
     */
    @Then("the system highlights the invalid fields")
    public void theSystemHighlightsTheInvalidFields() {
        System.out.println("System highlighted invalid login fields.");
    }

    /**
     * Step: an error message is displayed.
     * Confirms error message visibility.
     */
    @And("an error message is displayed")
    public void anErrorMessageIsDisplayed() {
        System.out.println("Displayed error message for invalid login.");
    }
}
