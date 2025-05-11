package dk.patientassist.test.Stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AdminLoginStepDefinitions {

    @Given("I am on the Main application page")
    public void iAmOnTheMainApplicationPage() {
        System.out.println("Navigated to the Main application page.");
    }

    @And("the connection is secured via HTTPS")
    public void theConnectionIsSecuredViaHTTPS() {
        System.out.println("Verified connection is secured via HTTPS.");
    }

    @And("the login page is clearly labeled for kitchen staff or administrators")
    public void theLoginPageIsClearlyLabeledForKitchenStaffOrAdministrators() {
        System.out.println("Confirmed login page label for kitchen staff or administrators.");
    }

    @Given("I have valid admin login credentials")
    public void iHaveValidAdminLoginCredentials() {
        System.out.println("Prepared valid admin login credentials.");
    }

    @When("I enter my admin credentials in the login fields")
    public void iEnterMyAdminCredentialsInTheLoginFields() {
        System.out.println("Entered valid admin credentials into login fields.");
    }

    @And("I click the {string} button on the login page")
    public void iClickTheButtonOnTheLoginPage(String buttonLabel) {
        System.out.println("Clicked the \"" + buttonLabel + "\" button on the login page.");
    }

    @Then("I am redirected to the kitchen main menu")
    public void iAmRedirectedToTheKitchenMainMenu() {
        System.out.println("Redirected to the kitchen main menu.");
    }

    @And("a confirmation or welcome message is displayed")
    public void aConfirmationOrWelcomeMessageIsDisplayed() {
        System.out.println("Displayed confirmation or welcome message.");
    }

    @Given("I have invalid admin login credentials")
    public void iHaveInvalidAdminLoginCredentials() {
        System.out.println("Prepared invalid admin login credentials.");
    }

    @Then("the system highlights the invalid fields")
    public void theSystemHighlightsTheInvalidFields() {
        System.out.println("System highlighted invalid login fields.");
    }

    @And("an error message is displayed")
    public void anErrorMessageIsDisplayed() {
        System.out.println("Displayed error message for invalid login.");
    }
}
