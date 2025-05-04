package dk.patientassist.stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginStepDefinitions {

    private Response response;
    private String jwtToken;
    private final String BASE_URI = "http://localhost:7070/api";

    @Given("I am a registered user with username {string} and password {string}")
    public void i_am_a_registered_user(String username, String password) {
        // Try to register user (might already exist, which is fine for test)
        given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(409))); // 200 OK or 409 Conflict (user exists)
    }

    @When("I send a POST request to {string} with my credentials")
    public void i_send_post_request_with_credentials(String endpoint) {
        response = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body(Map.of("username", "chef", "password", "password123"))
                .when()
                .post(endpoint);
        if (response.statusCode() == 200) {
            jwtToken = response.jsonPath().getString("token");
        }
    }

    @Then("I receive a {int} OK response")
    public void i_receive_a_ok_response(Integer expectedCode) {
        response.then().statusCode(expectedCode);
    }

    @And("I receive a valid JWT token")
    public void i_receive_a_valid_jwt_token() {
        assertThat(jwtToken, is(notNullValue()));
        assertThat(jwtToken.length(), is(greaterThan(10)));
    }

    @Given("I am not registered")
    public void i_am_not_registered() {
        // Precondition only; no-op
    }

    @When("I send a POST request to {string} with username {string} and password {string}")
    public void i_send_post_request_with_invalid_credentials(String endpoint, String username, String password) {
        response = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when()
                .post(endpoint);
    }

    @Then("I receive a {int} Unauthorized response")
    public void i_receive_a_unauthorized_response(Integer code) {
        response.then().statusCode(code);
    }

    @Given("I am logged in as {string} with role {string}")
    public void i_am_logged_in_with_role(String username, String role) {
        // Log in to get JWT
        i_am_a_registered_user(username, "password123");

        response = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", "password123"))
                .when()
                .post("/auth/login");

        jwtToken = response.jsonPath().getString("token");

        // Optionally add role (this might need a secure endpoint)
        given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .body(Map.of("username", username, "role", role))
                .when()
                .post("/auth/user/addrole")
                .then()
                .statusCode(anyOf(is(200), is(400))); // Already has role is OK
    }

    @When("I send a POST request to {string} with valid dish data")
    public void i_send_post_request_with_dish_data(String endpoint) {
        String dishJson = """
            {
              "name": "Ratatouille",
              "description": "Grøntsagsret",
              "availableFrom": "2025-05-01",
              "availableUntil": "2025-06-01",
              "status": "TILGÆNGELIG",
              "kcal": 400.0,
              "protein": 15.0,
              "carbohydrates": 35.0,
              "fat": 12.0,
              "allergens": "LACTOSE"
            }
        """;

        response = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .body(dishJson)
                .when()
                .post(endpoint);
    }

    @Then("I receive a {int} Created response")
    public void i_receive_created_response(Integer code) {
        response.then().statusCode(code);
    }

    @Then("I receive a {int} Forbidden response")
    public void i_receive_forbidden_response(Integer code) {
        response.then().statusCode(code);
    }
}