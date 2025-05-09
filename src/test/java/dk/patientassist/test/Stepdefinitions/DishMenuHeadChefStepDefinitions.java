package dk.patientassist.test.Stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DishMenuHeadChefStepDefinitions {

    private static String jwtToken;
    private Response response;
    private Map<String, Object> dishPayload = new HashMap<>();
    private final String BASE_URL = "http://localhost:7070/api/dishes";

    @Given("I am logged in as head chef")
    public void iAmLoggedInAsHeadChef() {
        String BASE_AUTH_URL = "http://localhost:7070/auth";

        // Register the user (you may want to skip this if user already exists)
        RestAssured.given()
                .contentType("application/json")
                .body("""
                    {
                        "username": "chef",
                        "password": "test123"
                    }
                  """)
                .post(BASE_AUTH_URL + "/register/");

        // Login and store token
        Response loginResponse = RestAssured.given()
                .contentType("application/json")
                .body("""
                    {
                        "username": "chef",
                        "password": "test123"
                    }
                  """)
                .post(BASE_AUTH_URL + "/login/");

        Assert.assertEquals("Login failed", 200, loginResponse.getStatusCode());

        String token = loginResponse.jsonPath().getString("token");
        Assert.assertNotNull("Token was null", token);

        // Set default auth header for all future requests
        RestAssured.requestSpecification = RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");

        System.out.println("I am logged in as head chef");
    }


    @And("I am on the {string} page")
    public void iAmOnThePage(String page) {
        System.out.println("I am on the " + page + " page");
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonText) {
        if (buttonText.equalsIgnoreCase("Gem")) {
            response = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType("application/json")
                    .body(dishPayload)
                    .post(BASE_URL + "/new");
        } else if (buttonText.equalsIgnoreCase("Fjern")) {
            int dishId = (int) dishPayload.get("id");
            response = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .delete(BASE_URL + "/" + dishId);
        }
    }

    @And("I enter {string} in the {string} field")
    public void iEnterInTheField(String value, String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "titel" -> dishPayload.put("name", value);
            case "beskrivelse" -> dishPayload.put("description", value);
            case "ernæringsinfo" -> {
                String[] parts = value.split(",");
                dishPayload.put("kcal", Double.parseDouble(parts[0].replaceAll("[^\\d.]", "")));
                dishPayload.put("protein", Double.parseDouble(parts[1].replaceAll("[^\\d.]", "")));
            }
            case "opskrift" -> {
                Map<String, Object> recipe = new HashMap<>();
                recipe.put("title", "Opskrift");
                recipe.put("instructions", value);
                recipe.put("ingredients", List.of(Map.of("name", "test ingredient")));
                dishPayload.put("recipe", recipe);
            }
        }
        dishPayload.putIfAbsent("carbohydrates", 0.0);
        dishPayload.putIfAbsent("fat", 0.0);
        dishPayload.putIfAbsent("allergens", List.of("GLUTEN"));
    }

    @Then("the system should validate the input")
    public void theSystemShouldValidateTheInput() {
        Assert.assertEquals(201, response.statusCode());
    }

    @And("the dish {string} should be added to the menu")
    public void theDishShouldBeAddedToTheMenu(String dishName) {
        String name = response.jsonPath().getString("name");
        Assert.assertEquals(dishName, name);
    }

    @And("I should see the message {string}")
    public void iShouldSeeTheMessage(String expectedMsg) {
        Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    }

    @And("I leave the {string} field empty")
    public void iLeaveTheFieldEmpty(String field) {
        switch (field.toLowerCase()) {
            case "titel" -> dishPayload.put("name", "");
            case "beskrivelse" -> dishPayload.put("description", "");
        }
    }

    @Then("the system should highlight the {string} field")
    public void theSystemShouldHighlightTheField(String field) {
        Assert.assertEquals(400, response.statusCode());
    }

    @And("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String errorMsg) {
        Assert.assertTrue(response.body().asString().contains(errorMsg));
    }

    @Given("the dish {string} exists in the menu")
    public void theDishExistsInTheMenu(String dishName) {
        response = RestAssured
                .given()
                .header("Authorization", "Bearer " + jwtToken)
                .get(BASE_URL);

        List<String> names = response.jsonPath().getList("name");
        Assert.assertNotNull(names);
        Assert.assertTrue(names.contains(dishName));
    }

    @When("I click on {string}")
    public void iClickOn(String dishName) {
        response = RestAssured
                .given()
                .header("Authorization", "Bearer " + jwtToken)
                .get(BASE_URL);

        var dishes = response.jsonPath().getList("", Map.class);
        var match = dishes.stream()
                .filter(d -> d.get("name").equals(dishName))
                .findFirst()
                .orElseThrow();

        dishPayload.put("id", match.get("id"));
    }

    @And("I confirm the removal in the dialog")
    public void iConfirmTheRemovalInTheDialog() {
        // No-op for backend tests
    }

    @Then("the dish {string} should be removed from the menu")
    public void theDishShouldBeRemovedFromTheMenu(String dishName) {
        Assert.assertEquals(200, response.statusCode());
    }

    @And("{string} should no longer be visible in the menu list")
    public void shouldNoLongerBeVisibleInTheMenuList(String dishName) {
        response = RestAssured
                .given()
                .header("Authorization", "Bearer " + jwtToken)
                .get(BASE_URL);

        List<String> names = response.jsonPath().getList("name");
        Assert.assertNotNull(names);
        Assert.assertFalse(names.contains(dishName));
    }

    @And("I change the {string} to {string}")
    public void iChangeTheTo(String field, String newValue) {
        int id = (int) dishPayload.get("id");
        String endpointField = switch (field.toLowerCase()) {
            case "titel" -> "name";
            case "beskrivelse" -> "description";
            case "tilgængelig fra" -> "available_from";
            case "tilgængelig til" -> "available_until";
            default -> field.toLowerCase();
        };

        response = RestAssured
                .given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("text/plain")
                .body(newValue)
                .patch(BASE_URL + "/" + id + "/" + endpointField);
    }

    @Then("the system should validate the updated input")
    public void theSystemShouldValidateTheUpdatedInput() {
        Assert.assertEquals(200, response.statusCode());
    }

    @And("the dish title should be updated to {string}")
    public void theDishTitleShouldBeUpdatedTo(String updatedName) {
        Assert.assertEquals(updatedName, response.jsonPath().getString("name"));
    }

    @And("I clear the {string} field")
    public void iClearTheField(String field) {
        switch (field.toLowerCase()) {
            case "titel" -> dishPayload.put("name", "");
            case "beskrivelse" -> dishPayload.put("description", "");
        }
    }
}
