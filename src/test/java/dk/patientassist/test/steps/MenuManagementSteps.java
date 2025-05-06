package dk.patientassist.test.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class MenuManagementSteps {

    private final String BASE_URI = "http://localhost:7070/api";
    private Response response;
    private Map<String, String> dishDetails = new HashMap<>();
    private String selectedDishName;

    @Given("I am logged in as head chef \\(admin)")
    public void i_am_logged_in_as_head_chef() {
        // Optional setup, or token setup if needed
        System.out.println("User is authenticated as head chef.");
    }

    @Given("I am on the {string} page")
    public void i_am_on_the_page(String pageName) {
        System.out.println("Navigated to page: " + pageName);
    }

    @When("I click the {string} button")
    public void i_click_the_button(String button) {
        System.out.println("Clicked button: " + button);
    }

    @When("I enter all required dish details:")
    public void i_enter_all_required_dish_details(DataTable table) {
        dishDetails = table.asMap(String.class, String.class);
    }

    @When("I click {string} to save the dish")
    public void i_click_save_to_add_dish(String buttonText) {
        response = given()
                .baseUri(BASE_URI)
                .basePath("/dishes")
                .contentType("application/json")
                .body(dishDetails)
                .when()
                .post();

        System.out.println("Saving new dish: " + dishDetails.get("titel"));
    }

    @Then("the system validates the input")
    public void the_system_validates_the_input() {
        assertNotNull(response);
        assertTrue(response.statusCode() == 200 || response.statusCode() == 400);
    }

    @Then("ensures all mandatory fields are filled")
    public void ensures_all_mandatory_fields_are_filled() {
        if (response.statusCode() == 400) {
            String error = response.jsonPath().getString("error");
            assertTrue(error.contains("obligatoriske felter"));
        }
    }

    @Then("the system adds the dish to the menu")
    public void the_system_adds_the_dish_to_the_menu() {
        assertEquals(200, response.statusCode(), "Expected 200 OK after adding dish");
    }

    @Then("a confirmation message is displayed: {string}")
    public void a_confirmation_message_is_displayed(String expectedMessage) {
        String actualMessage = response.jsonPath().getString("message");
        assertNotNull(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @When("I locate and click on the dish {string}")
    public void i_locate_and_click_on_the_dish(String dishName) {
        selectedDishName = dishName;
        System.out.println("Selected dish: " + dishName);
    }

    @When("I click the {string} button")
    public void i_click_the_remove_button(String buttonText) {
        if ("Fjern".equalsIgnoreCase(buttonText)) {
            response = given()
                    .baseUri(BASE_URI)
                    .basePath("/dishes/" + selectedDishName)
                    .when()
                    .delete();
        }
    }

    @When("I confirm the removal in a pop-up dialog: {string}")
    public void i_confirm_removal(String confirmationText) {
        System.out.println("Confirmed popup: " + confirmationText);
    }

    @Then("the system removes the dish from the menu")
    public void the_system_removes_the_dish_from_the_menu() {
        assertEquals(200, response.statusCode(), "Expected 200 OK after removing dish");
    }

    @Then("the dish {string} no longer appears in the menu list")
    public void the_dish_no_longer_appears_in_the_menu_list(String dishName) {
        Response getResponse = given()
                .baseUri(BASE_URI)
                .basePath("/dishes")
                .when()
                .get();

        String responseBody = getResponse.getBody().asString();
        assertFalse(responseBody.contains(dishName), "Dish should no longer be in menu");
    }

    @When("I click the {string} button")
    public void i_click_the_edit_button(String buttonText) {
        System.out.println("Clicked: " + buttonText);
    }

    @When("I modify the dish details:")
    public void i_modify_the_dish_details(DataTable table) {
        dishDetails = table.asMap(String.class, String.class);

        response = given()
                .baseUri(BASE_URI)
                .basePath("/dishes/" + selectedDishName)
                .contentType("application/json")
                .body(dishDetails)
                .when()
                .put();
    }

    @Then("the system validates the updated input")
    public void the_system_validates_the_updated_input() {
        assertNotNull(response);
        assertTrue(response.statusCode() == 200 || response.statusCode() == 400);
    }

    @Then("ensures all mandatory fields are still filled")
    public void ensures_all_mandatory_fields_are_still_filled() {
        if (response.statusCode() == 400) {
            String error = response.jsonPath().getString("error");
            assertTrue(error.contains("obligatoriske felter"));
        }
    }

    @Then("checks for invalid characters or formatting")
    public void checks_for_invalid_characters_or_formatting() {
        if (response.statusCode() == 400) {
            String error = response.jsonPath().getString("error");
            assertFalse(error.contains("<script>"), "Should not allow script injection");
        }
    }

    @Then("the system saves the changes")
    public void the_system_saves_the_changes() {
        assertEquals(200, response.statusCode(), "Expected 200 OK after editing dish");
    }
}
