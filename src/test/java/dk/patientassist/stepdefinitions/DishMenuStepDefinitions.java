package dk.patientassist.stepdefinitions;

import dk.patientassist.control.DishController;
import dk.patientassist.control.OrderController;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dao.OrderDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import io.cucumber.java.en.*;
import org.junit.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DishMenuStepDefinitions {

    private final DishController dishController;
    private final OrderController orderController;
    private final DishDAO dishDAO;
    private final OrderDAO orderDAO;
    private Integer orderId;
    private DishDTO dishDTO;
    private OrderDTO orderDTO;

    public DishMenuStepDefinitions() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        this.dishController = new DishController();
        this.orderController = new OrderController();
        this.dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        this.orderDAO = OrderDAO.getInstance(HibernateConfig.getEntityManagerFactory());

        this.dishDTO = new DishDTO(
                "Kylling i karry",
                "Godt med karry",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                DishStatus.TILGÆNGELIG,
                600.0,
                25.0,
                50.0,
                10.0,
                Allergens.GLUTEN
        );

        this.orderDTO = new OrderDTO(
                201,
                LocalDateTime.now(),
                "Ingen allergier",
                dishDTO,
                OrderStatus.VENTER
        );
    }

    @Given("the patient has an assigned bed number and an iPad")
    public void thePatientHasAnAssignedBedNumberAndAnIPad() {
        // No-op; precondition only
    }

    @And("the patient has opened the Menu on the iPad")
    public void thePatientHasOpenedTheMenuOnTheIPad() {
        dishController.getAllAvailableDishes();
    }

    @When("the patient selects a dish from the Menu")
    public void thePatientSelectsADishFromTheMenu() {
        // Task 2 - implemented elsewhere
    }

    @And("the patient clicks the {string} button")
    public void thePatientClicksTheButton(String button) {
        // Task 2 - implemented elsewhere
    }

    @Then("a confirmation message should be displayed on the screen.")
    public void aConfirmationMessageShouldBeDisplayedOnTheScreen() {
        // Task 3 - frontend only
    }

    @Given("the patient has placed an order")
    public void thePatientHasPlacedAnOrder() {
        DishDTO savedDish = dishDAO.createDish(dishDTO);
        orderDTO.setDish(savedDish);
        OrderDTO savedOrder = orderDAO.createOrder(orderDTO);
        this.orderId = savedOrder.getId();
    }

    @And("the patient regrets the order or needs to make changes to it")
    public void thePatientRegretsTheOrderOrNeedsToMakeChangesToIt() {
        // Placeholder
    }

    @When("the patient chooses to cancel the order before the deadline")
    public void thePatientChoosesToCancelTheOrderBeforeTheDeadline() {
        // Placeholder
    }

    @And("the patient presses the {string} button")
    public void thePatientPressesTheButton(String button) {
        orderController.cancelOrder(orderId);
    }

    @Then("the order will be cancelled")
    public void theOrderWillBeCancelled() {
        OrderDTO order = orderController.getOrder(orderId);
        Assert.assertEquals(OrderStatus.ANNULLERET, order.getStatus());
    }

    @And("the system will be updated.")
    public void theSystemWillBeUpdated() {
        // See previous step
    }

    @Given("a dish is sold out")
    public void aDishIsSoldOut() {
        // Task 5
    }

    @When("the patient views the list of available dishes")
    public void thePatientViewsTheListOfAvailableDishes() {
        // Task 5
    }

    @Then("it should be clearly indicated \\(e.g. with a “Sold Out” label) that the dish cannot be ordered")
    public void itShouldBeClearlyIndicatedThatTheDishCannotBeOrdered() {
        // Task 5
    }
}
