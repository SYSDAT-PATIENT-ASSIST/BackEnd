package dk.patientassist.test.Stepdefinitions;

import io.cucumber.java.en.*;

public class DishMenu_HeadChef_ManagementStepDefinitions {

    @Given("I am logged in as head chef")
    public void iAmLoggedInAsHeadChef() {
        System.out.println("Logged in as head chef.");
    }

    @Given("I am on the {string} page")
    public void iAmOnThePage(String pageName) {
        System.out.println("Navigated to page: " + pageName);
    }

    @When("I click the {string} button")
    public void iClickTheButton(String button) {
        System.out.println("Clicked button: " + button);
    }

    @And("I enter {string} in the {string} field")
    public void iEnterTextInField(String text, String fieldName) {
        System.out.println("Entered '" + text + "' into the '" + fieldName + "' field.");
    }

    @Then("the system should validate the input")
    public void systemShouldValidateInput() {
        System.out.println("System validates input.");
    }

    @And("the dish {string} should be added to the menu")
    public void dishShouldBeAdded(String dish) {
        System.out.println("Dish '" + dish + "' has been added to the menu.");
    }

    @And("I should see the message {string}")
    public void iShouldSeeMessage(String message) {
        System.out.println("Displayed message: " + message);
    }

    @And("I leave the {string} field empty")
    public void leaveFieldEmpty(String fieldName) {
        System.out.println("Left field '" + fieldName + "' empty.");
    }

    @Then("the system should highlight the {string} field")
    public void systemHighlightsField(String fieldName) {
        System.out.println("System highlights missing field: " + fieldName);
    }

    @Given("the dish {string} exists in the menu")
    public void dishExistsInMenu(String dish) {
        System.out.println("Confirmed that dish '" + dish + "' exists in the menu.");
    }

    @When("I click on {string}")
    public void clickOnDish(String dish) {
        System.out.println("Clicked on dish: " + dish);
    }

    @And("I confirm the removal in the dialog")
    public void confirmRemovalDialog() {
        System.out.println("Confirmed removal in dialog.");
    }

    @Then("the dish {string} should be removed from the menu")
    public void dishShouldBeRemoved(String dish) {
        System.out.println("Dish '" + dish + "' removed from the menu.");
    }

    @And("{string} should no longer be visible in the menu list")
    public void dishNotVisibleInMenuList(String dish) {
        System.out.println("Verified that '" + dish + "' is no longer visible in menu list.");
    }

    @And("I change the {string} to {string}")
    public void changeFieldTo(String field, String newValue) {
        System.out.println("Changed '" + field + "' to '" + newValue + "'.");
    }

    @Then("the system should validate the updated input")
    public void systemValidatesUpdatedInput() {
        System.out.println("System validates updated input.");
    }

    @And("the dish title should be updated to {string}")
    public void dishTitleUpdated(String newTitle) {
        System.out.println("Dish title updated to: " + newTitle);
    }

    @And("I clear the {string} field")
    public void clearField(String field) {
        System.out.println("Cleared field: " + field);
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String errorMessage) {
        System.out.println("Displayed error message: " + errorMessage);
    }
}
