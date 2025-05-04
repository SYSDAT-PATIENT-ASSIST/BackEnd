package dk.patientassist.stepdefinitions;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import org.junit.Assert;

import java.time.LocalDate;
import java.util.List;

public class DishManagementStepDefinitions {

    private final DishController controller;
    private final DishDAO dao;
    private DishDTO createdDish;
    private int responseStatus;
    private Exception thrownException;
    private List<DishDTO> dishList;

    public DishManagementStepDefinitions() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        controller = new DishController();
        dao = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());
    }

    @Before
    public void resetDatabaseBeforeScenario() {
        dao.getAllAvailableDishes().forEach(dish -> dao.deleteDish(dish.getId()));
    }

    @When("I create a dish with name {string} and status {string}")
    public void iCreateDishWithNameAndStatus(String name, String status) {
        try {
            DishDTO dto = new DishDTO(
                    name,
                    "Test dish",
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    DishStatus.valueOf(status.toUpperCase()),
                    400, 25, 30, 10,
                    Allergens.GLUTEN
            );
            createdDish = dao.createDish(dto);
            responseStatus = 201;
        } catch (Exception e) {
            thrownException = e;
            responseStatus = 400;
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expected) {
        Assert.assertEquals(expected, responseStatus);
    }

    @And("the dish name should be {string}")
    public void theDishNameShouldBe(String expectedName) {
        Assert.assertNotNull(createdDish);
        Assert.assertEquals(expectedName, createdDish.getName());
    }

    @When("I update dish {string} to status {string}")
    public void iUpdateDishToStatus(String name, String newStatus) {
        try {
            DishDTO original = dao.getAllAvailableDishes().stream()
                    .filter(d -> d.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            DishDTO updated = new DishDTO(
                    original.getName(),
                    original.getDescription(),
                    original.getAvailable_from(),
                    original.getAvailable_until(),
                    DishStatus.valueOf(newStatus.toUpperCase()),
                    original.getKcal(),
                    original.getProtein(),
                    original.getCarbohydrates(),
                    original.getFat(),
                    original.getAllergens()
            );

            createdDish = dao.updateDish(original.getId(), updated);
            responseStatus = 200;
        } catch (Exception e) {
            thrownException = e;
            responseStatus = 400;
        }
    }

    @Then("the updated dish status should be {string}")
    public void theUpdatedDishStatusShouldBe(String expected) {
        Assert.assertNotNull(createdDish);
        Assert.assertEquals(DishStatus.valueOf(expected.toUpperCase()), createdDish.getStatus());
    }

    @When("I try to create a dish with status {string}")
    public void iTryToCreateADishWithStatus(String status) {
        try {
            DishDTO dto = new DishDTO(
                    "Invalid Dish",
                    "Bad enum test",
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    DishStatus.valueOf(status.toUpperCase()),  // This will throw
                    300, 20, 25, 8,
                    Allergens.FISH
            );
            dao.createDish(dto);
            responseStatus = 201;
        } catch (IllegalArgumentException e) {
            thrownException = e;
            responseStatus = 400;
        }
    }

    @When("I fetch all available dishes")
    public void iFetchAllAvailableDishes() {
        try {
            dishList = controller.getAllAvailableDishes();
            responseStatus = 200;
        } catch (Exception e) {
            responseStatus = 500;
        }
    }

    @Then("the list should contain at least one dish")
    public void theListShouldContainAtLeastOneDish() {
        Assert.assertNotNull(dishList);
        Assert.assertFalse(dishList.isEmpty());
    }
}
