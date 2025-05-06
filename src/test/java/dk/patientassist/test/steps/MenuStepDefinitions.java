package dk.patientassist.test.steps;

import dk.patientassist.test.api.DishApiClient;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MenuStepDefinitions {

    private Response response;
    private String lastDishName;
    private String lastDishId;

    @Before
    public void setup() {
        lastDishName = null;
        lastDishId = null;
    }

    @Given("I am logged in as head chef")
    public void iAmLoggedInAsHeadChef() {
        System.out.println("Logged in as head chef (simulated or authenticated already)");
    }

    @Given("I am on the {string} page")
    public void iAmOnThePage(String pageName) {
        System.out.printf("Navigating to page: %s%n", pageName);
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonLabel) {
        switch (buttonLabel.toLowerCase()) {
            case "tilføj":
            case "gem":
            case "rediger":
                // No direct API action required here
                break;
            case "fjern":
                response = DishApiClient.deleteDish(lastDishName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported button: " + buttonLabel);
        }
    }

    @When("I enter the following dish details:")
    public void iEnterTheFollowingDishDetails(DataTable dataTable) {
        Map<String, String> dish = dataTable.asMaps().get(0);
        lastDishName = dish.get("name");

        response = DishApiClient.createDish(dish);
        response.then().statusCode(anyOf(is(200), is(201)));

        lastDishId = response.jsonPath().getString("id");
        assertThat("Dish ID must not be null", lastDishId, not(isEmptyOrNullString()));
    }

    @Then("the dish is saved")
    public void theDishIsSaved() {
        assertThat("Expected HTTP status 201 Created", response.statusCode(), is(201));
        assertNotNull(lastDishId, "Dish ID should have been captured during creation.");
    }

    @Then("a confirmation message {string} is displayed")
    public void aConfirmationMessageIsDisplayed(String expectedMessage) {
        String actualMessage = response.jsonPath().getString("message");
        assertThat("Expected confirmation message not found", actualMessage, containsString(expectedMessage));
    }

    @When("I select the dish named {string}")
    public void iSelectTheDishNamed(String dishName) {
        lastDishName = dishName;
    }

    @When("I confirm the removal")
    public void iConfirmTheRemoval() {
        assertNotNull(lastDishName, "Dish name must be selected before confirming removal.");
    }

    @Then("the dish is deleted")
    public void theDishIsDeleted() {
        assertThat("Expected successful deletion (200)", response.statusCode(), is(200));
    }

    @Then("the dish {string} is no longer in the menu")
    public void theDishIsNoLongerInTheMenu(String dishName) {
        Response getResponse = DishApiClient.getDish(dishName);
        assertThat("Dish should no longer exist", getResponse.statusCode(), is(404));
    }

    @When("I update the dish details:")
    public void iUpdateTheDishDetails(DataTable table) {
        Map<String, String> updates = table.asMaps().get(0);
        response = DishApiClient.updateDish(lastDishName, updates);
        response.then().statusCode(200);
    }

    @Then("the dish is updated successfully")
    public void theDishIsUpdatedSuccessfully() {
        assertThat("Expected HTTP 200 OK after update", response.statusCode(), is(200));
    }

    @When("I patch the field {string} of {string} with value {string}")
    public void iPatchTheFieldOfDish(String field, String dish, String value) {
        response = DishApiClient.patchDish(dish, Map.of(field, value));
        response.then().statusCode(200);
    }

    @Then("the dish {string} should have field {string} equal to {string}")
    public void theDishShouldHaveFieldEqualTo(String dish, String field, String expectedValue) {
        Response getResponse = DishApiClient.getDish(dish);
        getResponse.then().statusCode(200);

        Object actual = getResponse.jsonPath().get(field);
        assertNotNull(actual, "Field " + field + " was not found on the dish.");
        assertEquals(expectedValue, String.valueOf(actual), "Field value mismatch.");
    }
}
