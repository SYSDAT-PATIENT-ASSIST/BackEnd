package dk.patientassist.stepdefinitions;
import dk.patientassist.control.DishController;
import dk.patientassist.control.OrderController;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import org.junit.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class menuStepDefinitions
{
    private DishController dishController = new DishController();
    private OrderController orderController = new OrderController();
    private Dish dish = new Dish("Kylling i karry", "godt med karry", LocalDate.ofYearDay(2025,24), LocalDate.now(), DishStatus.AVAILABLE); //id, name, description, available_from, available_until, status
    private Order order = new Order(201, LocalDateTime.now(), "Ingen allergier", dish, OrderStatus.PENDING); //id, bed_id, order_time, note, dish, status

    @Given("the patient has an assigned bed number and an iPad")
    public void thePatientHasAnAssignedBedNumberAndAnIPad()
    {

    }

    @And("the patient has opened the Menu on the iPad")
    public void thePatientHasOpenedTheMenuOnTheIPad()
    {
        dishController.getAllAvailable();
    }

    @When("the patient selects a dish from the Menu")
    public void thePatientSelectsADishFromTheMenu()
    {
        // This step will be implemented by another team member - belongs to Task 2.
    }

    @And("the patient clicks the 'Bestil' button")
    public void thePatientClicksTheButton()
    {
        // This step will be implemented by another team member - belongs to Task 2.
    }

    @Then("a confirmation message should be displayed on the screen.")
    public void aConfirmationMessageShouldBeDisplayedOnTheScreen()
    {
        // Task 3. This is frontend only
    }

    @Given("the patient has placed an order")
    public void thePatientHasPlacedAnOrder()
    {
        Integer id = order.getId();
    }

    @And("the patient regrets the order or needs to make changes to it")
    public void thePatientRegretsTheOrderOrNeedsToMakeChangesToIt()
    {
        //see @And("the patient presses the 'Afbestil' button")
    }

    @When("the patient chooses to cancel the order before the deadline")
    public void thePatientChoosesToCancelTheOrderBeforeTheDeadline()
    {
        //see @And("the patient presses the 'Afbestil' button")
    }

    @And("the patient presses the 'Afbestil' button")
    public void thePatientPressesTheButton()
    {
        Integer id = order.getId();
        orderController.cancelOrder(id);
    }

    @Then("the order will be cancelled")
    public void theOrderWillBeCancelled()
    {
        OrderDTO order = orderController.getOrder(1);
        Assert.assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @And("the system will be updated.")
    public void theSystemWillBeUpdated()
    {
        // see @Then("the order will be cancelled")
    }

    @Given("a dish is sold out")
    public void aDishIsSoldOut()
    {
        // This step will be implemented by another team member - belongs to Task 5.
    }

    @When("the patient views the list of available dishes")
    public void thePatientViewsTheListOfAvailableDishes()
    {
        // This step will be implemented by another team member - belongs to Task 5.
    }

    @Then("it should be clearly indicated (e.g. with a “Sold Out” label) that the dish cannot be ordered")
    public void itShouldBeClearlyIndicatedEGWithASoldOutLabelThatTheDishCannotBeOrdered()
    {
        // This step will be implemented by another team member - belongs to Task 5.
    }
}
