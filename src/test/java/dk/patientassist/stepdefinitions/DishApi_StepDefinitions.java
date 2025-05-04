package dk.patientassist.stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DishApi_StepDefinitions {

    private Response response; // Holds the latest API response
    private String createdDishName = "Default Dish"; // Name used for creating dishes
    private Integer createdDishId; // ID of the dish created via POST

    // Set the base URI for the REST API
    static {
        RestAssured.baseURI = "http://localhost:7070/api";
    }

    // Step: Define a new dish name to be used in subsequent steps
    @Given("I have a new dish with name {string}")
    public void i_have_a_new_dish_with_name(String name) {
        this.createdDishName = name;
    }

    // Step: Send a POST request to create a dish
    @When("I send a POST request to {string}")
    public void i_send_a_post_request_to(String endpoint) {
        // JSON payload with placeholder values and injected name
        String jsonPayload = String.format("""
            {
              "name": "%s",
              "description": "Delicious dish",
              "availableFrom": "2025-05-01",
              "availableUntil": "2025-06-01",
              "status": "TILGÆNGELIG",
              "kcal": 600.0,
              "protein": 25.0,
              "carbohydrates": 50.0,
              "fat": 10.0,
              "allergens": "GLUTEN"
            }
        """, createdDishName);

        // Execute the POST request
        response = given()
                .contentType(JSON)
                .body(jsonPayload)
                .when()
                .post(endpoint);

        // Save the returned dish ID if creation was successful
        if (response.statusCode() == 201) {
            createdDishId = response.jsonPath().getInt("id");
        }
    }

    // Step: Verify the status code is 201
    @Then("I receive a {int} Created response")
    public void i_receive_a_created_response(Integer expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    // Step: Check if the dish appears in GET /dishes response
    @And("the dish is available when I fetch all dishes")
    public void the_dish_is_available_when_i_fetch_all_dishes() {
        Response getResponse = given()
                .accept(JSON)
                .when()
                .get("/dishes");

        getResponse.then().statusCode(200);

        List<Map<String, Object>> dishes = getResponse.jsonPath().getList("$");

        boolean found = dishes.stream()
                .anyMatch(d -> createdDishName.equals(d.get("name")));

        assertThat("Dish should be found in GET /dishes", found, is(true));
    }

    // Step: Patch the dish name
    @When("I update the dish name to {string}")
    public void i_update_the_dish_name_to(String newName) {
        String endpoint = "/dishes/" + createdDishId + "/name";
        String body = String.format("\"%s\"", newName);

        response = given()
                .contentType(JSON)
                .body(body)
                .when()
                .patch(endpoint);

        if (response.statusCode() == 200) {
            createdDishName = newName;
        }
    }

    // Step: Confirm name update success
    @Then("the dish name is updated successfully")
    public void the_dish_name_is_updated_successfully() {
        response.then().statusCode(200);
        String returnedName = response.jsonPath().getString("name");
        assertThat(returnedName, equalTo(createdDishName));
    }

    // Step: Replace the full dish using PUT
    @When("I replace the dish details using PUT")
    public void i_replace_the_dish_details_using_put() {
        String endpoint = "/dishes/" + createdDishId;

        String updatedJson = String.format("""
            {
              "id": %d,
              "name": "%s",
              "description": "Updated description",
              "availableFrom": "2025-05-01",
              "availableUntil": "2025-06-30",
              "status": "TILGÆNGELIG",
              "kcal": 700.0,
              "protein": 30.0,
              "carbohydrates": 40.0,
              "fat": 15.0,
              "allergens": "LACTOSE"
            }
        """, createdDishId, createdDishName);

        response = given()
                .contentType(JSON)
                .body(updatedJson)
                .when()
                .put(endpoint);
    }

    // Step: Verify PUT succeeded and description updated
    @Then("the dish is replaced successfully")
    public void the_dish_is_replaced_successfully() {
        response.then().statusCode(200);
        String description = response.jsonPath().getString("description");
        assertThat(description, containsString("Updated"));
    }

    // Step: Send DELETE request using dynamic ID
    @When("I send a DELETE request to \"/dishes/{int}\"")
    public void i_send_a_delete_request_to_dishes_with_id(Integer id) {
        response = when().delete("/dishes/" + id);
    }

    // Step: Verify DELETE response is 204
    @Then("the dish is deleted successfully")
    public void the_dish_is_deleted_successfully() {
        response.then().statusCode(204);
    }

    // Step: Confirm deleted dish cannot be retrieved
    @And("the dish no longer exists")
    public void the_dish_no_longer_exists() {
        Response getResponse = when().get("/dishes/" + createdDishId);
        getResponse.then().statusCode(404);
    }
}
