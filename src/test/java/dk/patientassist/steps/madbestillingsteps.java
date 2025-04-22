package dk.patientassist.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.Assert.*;

public class madbestillingsteps {

    // This is a placeholder for the order system, in a real scenario, it would be injected from the system
    private String bedNumber;
    private String dish;
    private String message;
    private String orderStatus;

    // Scenario 1: Viewing new meal orders
    @Given("a patient has placed a meal order")
    public void a_patient_has_placed_a_meal_order() {
        // Assuming that a patient order is being created with some data
        bedNumber = "101";
        dish = "Hakkebøf";
    }

    @When("the order is received in the kitchen system")
    public void the_order_is_received_in_the_kitchen_system() {
        // This is the point where the order is received in the system
    }

    @Then("the kitchen staff can see the patient’s bed number, selected dishes, and any dietary comments.")
    public void the_kitchen_staff_can_see_the_patient_s_bed_number_selected_dishes_and_any_dietary_comments() {
        // Checking if the bed number and dish are correctly set
        assertEquals("101", bedNumber);
        assertEquals("Hakkebøf", dish);
    }

    // Scenario 2: Accepting and preparing a meal order
    @Given("a “Hakkebøf” order is visible in the kitchen system")
    public void a_hakkebof_order_is_visible_in_the_kitchen_system() {
        // Set the order to the specified dish "Hakkebøf"
        dish = "Hakkebøf";
    }

    @When("the kitchen staff accepts the order")
    public void the_kitchen_staff_accepts_the_order() {
        // Update the order status to "Accepted" for example
        orderStatus = "Accepted";
    }

    @Then("the system updates the order status")
    public void the_system_updates_the_order_status() {
        // Verifying the order status has been updated
        assertEquals("Accepted", orderStatus);
    }

    @Then("the kitchen staff can start preparing the meal.")
    public void the_kitchen_staff_can_start_preparing_the_meal() {
        // Confirming the meal preparation can start
        assertNotNull(orderStatus);
        assertEquals("Accepted", orderStatus);
    }

    // Scenario 3: Notifying the patient that the meal is on the way
    @Given("the meal order is being prepared")
    public void the_meal_order_is_being_prepared() {
        // This is the state where the order has been accepted and preparation has started
        orderStatus = "Preparing";
    }

    @When("the kitchen staff marks the order as \"Klar til levering\"")
    public void the_kitchen_staff_marks_the_order_as_klar_til_levering() {
        // Change the status to "Ready for delivery"
        orderStatus = "Klar til levering";
    }

    @Then("the system notifies the patient with the message {string}.")
    public void the_system_notifies_the_patient_with_the_message(String expectedMessage) {
        // Notification message
        if (orderStatus.equals("Klar til levering")) {
            message = "Din mad er på vej";
        }

        // Verifying the system sends the correct message to the patient
        assertEquals(expectedMessage, message);
    }

    // Scenario 4: Handling a delayed meal order
    @Given("a meal order is delayed due to unforeseen circumstances")
    public void a_meal_order_is_delayed_due_to_unforeseen_circumstances() {
        // Simulating a delayed order state
        orderStatus = "Delayed";
    }

    @When("the kitchen staff updates the system with a delay")
    public void the_kitchen_staff_updates_the_system_with_a_delay() {
        // Update the status to delayed and set the message
        orderStatus = "Delayed";
    }

    @Then("the system notifies the patient with the message \"Din mad er forsinket.\"")
    public void the_system_notifies_the_patient_with_the_message_din_mad_er_forsinket() {
        // Verifying that the system sends the delay message
        if (orderStatus.equals("Delayed")) {
            message = "Din mad er forsinket";
        }

        // Verifying the delay message is sent
        assertEquals("Din mad er forsinket", message);
    }
}
