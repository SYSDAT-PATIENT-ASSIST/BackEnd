package dk.patientassist.stepdefinitions;

import dk.patientassist.control.DishController;
import dk.patientassist.control.OrderController;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dao.OrderDAO;
import dk.patientassist.persistence.dto.DishDTO;
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
import java.util.List;

public class menuStepDefinitions {
    private DishController dishController;
    private OrderController orderController;
    private DishDAO dishDAO;
    private OrderDAO orderDAO;
    private Integer orderId;
    private DishDTO dishDTO;
    private OrderDTO orderDTO;

    public menuStepDefinitions() {
        HibernateConfig.init(Mode.TEST);

        dishController = new DishController();
        orderController = new OrderController();
        dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        orderDAO = OrderDAO.getInstance(HibernateConfig.getEntityManagerFactory());
        dishDTO = new DishDTO("Kylling i karry", "godt med karry", LocalDate.now(), LocalDate.now(),
                DishStatus.TILGÆNGELIG); // id, name, description, available_from, available_until, status
        orderDTO = new OrderDTO(201, LocalDateTime.now(), "Ingen allergier", dishDTO, OrderStatus.VENTER); // id,
                                                                                                           // bed_id,
                                                                                                           // order_time,
                                                                                                           // note,
                                                                                                           // dish,
                                                                                                           // status
    }

    @Given("the patient has an assigned bed number and an iPad")
    public void thePatientHasAnAssignedBedNumberAndAnIPad() {

    }

    @And("the patient has opened the Menu on the iPad")
    public void thePatientHasOpenedTheMenuOnTheIPad() {
        // List<DishDTO> dishes = dishController.getAllAvailableToPatient();
    }

    @When("the patient selects a dish from the Menu")
    public void thePatientSelectsADishFromTheMenu() {
        // Task 2. This step is frontend only
    }

    @And("the patient clicks the {string} button")
    public void thePatientClicksTheButton(String button) {
        DishDTO savedDish = dishDAO.create(dishDTO);
        orderDTO.setDish(savedDish);
        OrderDTO savedOrder = orderController.createOrder(orderDTO);
        this.orderId = savedOrder.getId();
    }

    @Then("a confirmation message should be displayed on the screen.")
    public void aConfirmationMessageShouldBeDisplayedOnTheScreen() {
        // Task 3. This step is frontend only
    }

    @Given("the patient has placed an order")
    public void thePatientHasPlacedAnOrder() {

        DishDTO savedDish = dishDAO.create(dishDTO);
        orderDTO.setDish(savedDish);
        OrderDTO savedOrder = orderDAO.createOrder(orderDTO);
        this.orderId = savedOrder.getId();
    }

    @And("the patient regrets the order or needs to make changes to it")
    public void thePatientRegretsTheOrderOrNeedsToMakeChangesToIt() {
        // see @And("the patient presses the 'Afbestil' button")
    }

    @When("the patient chooses to cancel the order before the deadline")
    public void thePatientChoosesToCancelTheOrderBeforeTheDeadline() {
        // see @And("the patient presses the 'Afbestil' button")
    }

    @And("the patient presses the {string} button")
    public void thePatientPressesTheButton(String button) {
        orderController.cancelOrder(orderId);
    }

    @Then("the order will be cancelled")
    public void theOrderWillBeCancelled() {
        OrderDTO order = orderController.getOrder(1);
        Assert.assertEquals(OrderStatus.ANNULLERET, order.getStatus());
    }

    @And("the system will be updated.")
    public void theSystemWillBeUpdated() {
        // see @Then("the order will be cancelled")
    }

    @Given("a dish is sold out")
    public void aDishIsSoldOut() {
        // This step will be implemented by another team member - belongs to Task 5.
    }

    @When("the patient views the list of available dishes")
    public void thePatientViewsTheListOfAvailableDishes() {
        // This step will be implemented by another team member - belongs to Task 5.
    }

    @Then("it should be clearly indicated \\(e.g. with a “Sold Out” label) that the dish cannot be ordered")
    public void itShouldBeClearlyIndicatedEGWithASoldOutLabelThatTheDishCannotBeOrdered() {
        // This step will be implemented by another team member - belongs to Task 5.
    }
}
