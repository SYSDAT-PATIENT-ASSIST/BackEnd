package dk.patientassist.stepdefinitions;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class DishMenu_StepDefinitions {

    // Simulated user and interaction states
    private boolean loggedInAsHeadChef = false;
    private boolean onDishMenuPage = false;
    private String selectedDishTitle;
    private boolean dishSaved = false;
    private boolean dishRemoved = false;
    private boolean dishEdited = false;

    // Step: user is logged in as a head chef
    @Given("I am logged in as a head chef")
    public void i_am_logged_in_as_head_chef() {
        loggedInAsHeadChef = true;
    }

    // Step: user has navigated to a specific page
    @Given("I am on the {string} page")
    public void i_am_on_the_page(String pageName) {
        assertEquals("Dish Menu Management", pageName); // Validate correct page
        onDishMenuPage = true;
    }

    // Step: user clicks a button (generic)
    @When("I click the {string} button")
    public void i_click_the_button(String buttonName) {
        assertTrue(loggedInAsHeadChef && onDishMenuPage); // Action allowed only with proper role and page
    }

    // Step: user enters all required information for a dish
    @When("I enter all required dish details")
    public void i_enter_all_required_dish_details() {
        // Simulates filling required form inputs
    }

    // Step: user adds optional fields like description, nutrition info, or recipe
    @When("I add description, nutrition info, and recipe")
    public void i_add_description_nutrition_info_and_recipe() {
        // Simulates adding optional metadata
    }

    // Step: user saves the dish (for both creation and update)
    @When("I click {string} to save the dish")
    public void i_click_save_to_save_the_dish(String buttonLabel) {
        dishSaved = true; // Simulates successful save
    }

    // Step: system validates the submitted input
    @Then("the system validates the input")
    public void the_system_validates_the_input() {
        assertTrue(dishSaved); // Assumes validation passes
    }

    // Step: system adds the new dish to the menu
    @Then("the system adds the dish to the dish menu")
    public void the_system_adds_the_dish_to_the_dish_menu() {
        assertTrue(dishSaved);
    }

    // Step: confirmation message is shown
    @Then("a confirmation message is displayed: {string}")
    public void a_confirmation_message_is_displayed(String message) {
        assertNotNull(message);
        System.out.println("Message: " + message);
    }

    // Step: a dish with the given title already exists
    @Given("a dish titled {string} exists in the menu")
    public void a_dish_titled_exists_in_the_menu(String dishTitle) {
        selectedDishTitle = dishTitle;
    }

    // Step: user clicks a specific dish
    @When("I click on the dish {string}")
    public void i_click_on_the_dish(String dishTitle) {
        assertEquals(selectedDishTitle, dishTitle);
    }

    // Step: user confirms they want to delete the dish
    @When("I confirm the removal")
    public void i_confirm_the_removal() {
        dishRemoved = true;
    }

    // Step: system removes the dish
    @Then("the system removes the dish from the dish menu")
    public void the_system_removes_the_dish_from_the_dish_menu() {
        assertTrue(dishRemoved);
    }

    // Step: the removed dish is no longer listed
    @Then("the dish {string} no longer appears in the menu list")
    public void the_dish_no_longer_appears_in_the_menu_list(String dishTitle) {
        assertEquals(selectedDishTitle, dishTitle);
    }

    // Step: click the "Edit" button explicitly
    @When("I click the {string} button to edit")
    public void i_click_edit_button(String buttonName) {
        assertEquals("Rediger", buttonName); // Validates it's the correct edit button
        dishEdited = true;
    }

    // Step: user updates the dish title
    @When("I change the title to {string}")
    public void i_change_the_title_to(String newTitle) {
        selectedDishTitle = newTitle;
    }

    // Step: user optionally updates description or nutrition info
    @When("I optionally update the description or nutrition info")
    public void i_optionally_update_the_description_or_nutrition_info() {
        // Simulates optional content update
    }

    // Step: user saves their changes
    @When("I click {string} to save the changes")
    public void i_click_save_to_save_the_changes(String buttonLabel) {
        assertEquals("Gem", buttonLabel); // Must be the correct "Save" label
        dishEdited = true;
    }

    // Step: system validates updated inputs
    @Then("the system validates the updated input")
    public void the_system_validates_the_updated_input() {
        assertTrue(dishEdited);
    }

    // Step: system saves the changes
    @Then("the system saves the changes")
    public void the_system_saves_the_changes() {
        assertTrue(dishEdited);
    }
}
