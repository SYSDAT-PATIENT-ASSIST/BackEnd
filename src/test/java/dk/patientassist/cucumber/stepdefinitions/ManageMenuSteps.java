package dk.patientassist.cucumber.stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for user story: managing hospital digital menu as head chef.
 * Covers dish creation, patching, deletion, and validation via the REST API.
 */
public class ManageMenuSteps {

    private final String baseUri = "http://localhost";
    private final int port = 7070;
    private final String basePath = "/api/dishes";

    private Response response;
    private String dishName;

    // ========== STEP DEFINITIONS ==========

    @Given("I am logged in as head chef")
    public void i_am_logged_in_as_head_chef() {
        // Assumes test auth context
    }

    @Given("I am on the {string} page")
    public void i_am_on_the_page(String pageName) {
        // No-op for backend test
    }

    @When("I click the {string} button")
    public void i_click_the_button(String button) {
        // No-op
    }

    @When("I enter the following dish details:")
    public void i_enter_the_following_dish_details(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        dishName = row.get("name");

        response = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath)
                .contentType("application/json")
                .body(buildDishJson(row))
                .when()
                .post();
    }

    @When("I click the \"Gem\" button")
    public void i_click_save() {
        // Already submitted
    }

    @Then("the dish is saved")
    public void the_dish_is_saved() {
        assertEquals(201, response.getStatusCode());
    }

    @Then("a confirmation message {string} is displayed")
    public void confirmation_message_displayed(String msg) {
        assertNotNull(response.jsonPath().getString("id"));
    }

    @When("I select the dish named {string}")
    public void i_select_dish_named(String dishName) {
        this.dishName = dishName;
    }

    @When("I click the \"Fjern\" button")
    public void i_click_delete_button() {
        int id = findDishIdByName(dishName);
        assertTrue(id > 0, "Dish not found for deletion");

        response = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath + "/" + id)
                .when()
                .delete();
    }

    @When("I confirm the removal")
    public void i_confirm_removal() {
        // Simulated in test
    }

    @Then("the dish is deleted")
    public void the_dish_is_deleted() {
        assertEquals(200, response.getStatusCode());
    }

    @Then("the dish {string} is no longer in the menu")
    public void dish_is_no_longer_listed(String name) {
        Response getAll = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath)
                .when()
                .get();

        assertFalse(getAll.getBody().asString().contains(name));
    }

    @When("I click the \"Rediger\" button")
    public void i_click_edit_button() {
        // no-op
    }

    @When("I update the dish details:")
    public void i_update_the_dish_details(io.cucumber.datatable.DataTable dataTable) {
        int id = findDishIdByName(dishName);
        assertTrue(id > 0);

        for (Map<String, String> row : dataTable.asMaps()) {
            String field = row.get("field");
            String value = row.get("value");

            response = given()
                    .baseUri(baseUri)
                    .port(port)
                    .basePath(basePath + "/" + id + "/" + field)
                    .contentType("application/json")
                    .body(formatPatchValue(field, value))
                    .when()
                    .patch();

            response.then().statusCode(200);
        }
    }

    @When("I patch the field {string} of {string} with value {string}")
    public void patch_single_field(String field, String name, String value) {
        int id = findDishIdByName(name);
        assertTrue(id > 0, "Dish not found for patching");

        response = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath + "/" + id + "/" + field)
                .contentType("application/json")
                .body(formatPatchValue(field, value))
                .when()
                .patch();
    }

    @Then("the dish is updated successfully")
    public void the_dish_is_updated_successfully() {
        assertEquals(200, response.getStatusCode());
    }

    @Then("the dish {string} should have field {string} equal to {string}")
    public void dish_field_should_equal(String dishName, String field, String expectedValue) {
        int id = findDishIdByName(dishName);

        Response get = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath + "/" + id)
                .when()
                .get();

        String actual = get.jsonPath().getString(field);
        assertEquals(expectedValue, actual, "Mismatch in field " + field);
    }

    // ========== UTILS ==========

    private String buildDishJson(Map<String, String> fields) {
        return String.format("""
            {
              "name": "%s",
              "description": "%s",
              "availableFrom": "2025-06-01",
              "availableUntil": "2025-06-30",
              "status": "%s",
              "kcal": %s,
              "protein": %s,
              "carbohydrates": %s,
              "fat": %s,
              "allergens": "%s"
            }
        """,
                fields.get("name"),
                fields.getOrDefault("description", ""),
                fields.getOrDefault("status", "TILGÆNGELIG"),
                fields.getOrDefault("kcal", "0"),
                fields.getOrDefault("protein", "0"),
                fields.getOrDefault("carbohydrates", "0"),
                fields.getOrDefault("fat", "0"),
                fields.getOrDefault("allergens", "GLUTEN")
        );
    }

    private Object formatPatchValue(String field, String value) {
        return switch (field) {
            case "kcal", "protein", "carbohydrates", "fat" -> Double.valueOf(value);
            case "status" -> value.toUpperCase();
            case "allergens" -> value.toUpperCase();
            default -> value;
        };
    }

    private int findDishIdByName(String name) {
        Response res = given()
                .baseUri(baseUri)
                .port(port)
                .basePath(basePath)
                .when()
                .get();

        return res.jsonPath().getInt("find { it.name == '%s' }.id".formatted(name));
    }

    @After
    public void logResponseOnFailure() {
        if (response != null && response.getStatusCode() >= 400) {
            System.err.println("❌ API Error Response:\n" + response.prettyPrint());
        }
    }
}
