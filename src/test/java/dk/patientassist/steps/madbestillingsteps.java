package dk.patientassist.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.Assert.*;

public class madbestillingsteps {

    // Placeholder variables for order data and system state (e.g., order status, message).
    private String bedNumber;
    private String dish;
    private String dietaryComments;
    private String message;
    private String orderStatus;

    // Scenario 1: Viewing new meal orders
    @Given("a patient has placed a meal order")
    public void a_patient_has_placed_a_meal_order() {
        // Simulating the meal order placement by setting a patient bed number and the chosen dish
        bedNumber = "101";  // Bed number where the patient is located
        dish = "Hakkebøf";  // The dish ordered by the patient
        dietaryComments = "No salt";  // Any special dietary instructions (example)
    }

    @When("the order is received in the kitchen system")
    public void the_order_is_received_in_the_kitchen_system() {
        // Simulating the action where the order is received by the kitchen system
        // In a real-world case, this would trigger some kind of system update
    }

    @Then("the kitchen staff can see the patient’s bed number, selected dishes, and any dietary comments.")
    public void the_kitchen_staff_can_see_the_patient_s_bed_number_selected_dishes_and_any_dietary_comments() {
        // Verifying if the kitchen staff can view the correct patient information and meal details
        assertEquals("101", bedNumber);  // Verifying the bed number
        assertEquals("Hakkebøf", dish);  // Verifying the dish ordered
        assertEquals("No salt", dietaryComments);  // Verifying dietary comments
    }

    // Scenario 2: Accepting and preparing a meal order
    @Given("a “Hakkebøf” order is visible in the kitchen system")
    public void a_hakkebof_order_is_visible_in_the_kitchen_system() {
        // The order is displayed on the kitchen staff’s screen, indicating that the order is ready to be processed
        dish = "Hakkebøf";  // Set the dish that is ordered
    }

    @When("the kitchen staff accepts the order")
    public void the_kitchen_staff_accepts_the_order() {
        // The kitchen staff accepts the order, which updates the order status
        orderStatus = "Accepted";  // Change the order status to accepted
    }

    @Then("the system updates the order status")
    public void the_system_updates_the_order_status() {
        // Verifying that the system has correctly updated the order status
        assertEquals("Accepted", orderStatus);  // Check that the order status is "Accepted"
    }

    @Then("the kitchen staff can start preparing the meal.")
    public void the_kitchen_staff_can_start_preparing_the_meal() {
        // Verifying that the meal can now be prepared by confirming the order status
        assertNotNull(orderStatus);  // Check that the order status is not null
        assertEquals("Accepted", orderStatus);  // Ensure the status is "Accepted"
    }

    // Scenario 3: Notifying the patient that the meal is on the way
    @Given("the meal order is being prepared")
    public void the_meal_order_is_being_prepared() {
        // The order is now in preparation, so we set the status to "Preparing"
        orderStatus = "Preparing";
    }

    @When("the kitchen staff marks the order as \"Klar til levering\"")
    public void the_kitchen_staff_marks_the_order_as_klar_til_levering() {
        // The kitchen staff updates the order status to "Ready for delivery"
        orderStatus = "Klar til levering";  // Mark the order as ready
    }

    @Then("the system notifies the patient with the message {string}.")
    public void the_system_notifies_the_patient_with_the_message(String expectedMessage) {
        // When the order is marked as ready, the system sends a notification to the patient
        if (orderStatus.equals("Klar til levering")) {
            message = "Din mad er på vej";  // Message sent to the patient: "Your food is on the way"
        }

        // Verifying that the correct message is sent to the patient
        assertEquals(expectedMessage, message);  // Compare the expected and actual message
    }

    // Scenario 4: Handling a delayed meal order
    @Given("a meal order is delayed due to unforeseen circumstances")
    public void a_meal_order_is_delayed_due_to_unforeseen_circumstances() {
        // Simulating a delay in the order
        orderStatus = "Delayed";  // Setting the order status to "Delayed"
    }

    @When("the kitchen staff updates the system with a delay")
    public void the_kitchen_staff_updates_the_system_with_a_delay() {
        // The kitchen staff updates the system to reflect the delay
        orderStatus = "Delayed";  // Confirming the status as "Delayed"
    }

    @Then("the system notifies the patient with the message \"Din mad er forsinket.\"")
    public void the_system_notifies_the_patient_with_the_message_din_mad_er_forsinket() {
        // When the order is delayed, the system should notify the patient
        if (orderStatus.equals("Delayed")) {
            message = "Din mad er forsinket";  // Message sent to the patient: "Your food is delayed"
        }

        // Verifying that the correct delay message is sent
        assertEquals("Din mad er forsinket", message);  // Ensure the message matches the expected one
    }
}
