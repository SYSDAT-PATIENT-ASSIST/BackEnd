package dk.patientassist.api;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DishControllerIntegrationTest {

    private static Javalin app;
    private static DishDAO dishDAO;
    private static int createdDishId;

    @BeforeAll
    static void setup() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        dishDAO = DishDAO.getInstance(HibernateConfig.getEntityManagerFactory());

        DishController controller = new DishController();

        app = Javalin.create();
        controller.registerRoutes(app); // registers all endpoints

        app.start(7071);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7071;
    }

    @AfterAll
    static void teardown() {
        if (app != null) app.stop();
    }

    @Test
    @Order(1)
    void shouldCreateDish() {
        String json = """
            {
              "name": "Integration Frikadeller",
              "description": "Smager godt",
              "availableFrom": "2025-05-05",
              "availableUntil": "2025-06-05",
              "status": "TILGÆNGELIG",
              "kcal": 550.0,
              "protein": 35.0,
              "carbohydrates": 45.0,
              "fat": 15.0,
              "allergens": "GLUTEN"
            }
        """;

        createdDishId = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/api/dishes")
                .then()
                .statusCode(201)
                .body("name", equalTo("Integration Frikadeller"))
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    void shouldFetchDishById() {
        given()
                .when()
                .get("/api/dishes/{id}", createdDishId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdDishId))
                .body("name", equalTo("Integration Frikadeller"));
    }

    @Test
    @Order(3)
    void shouldFetchAllDishes() {
        given()
                .when()
                .get("/api/dishes")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(4)
    void shouldUpdateDishKcal() {
        given()
                .contentType("application/json")
                .body("999.0")
                .when()
                .patch("/api/dishes/{id}/kcal", createdDishId)
                .then()
                .statusCode(200)
                .body("kcal", equalTo(999.0f));
    }

    @Test
    @Order(5)
    void shouldDeleteDish() {
        given()
                .when()
                .delete("/api/dishes/{id}", createdDishId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    void shouldReturn404ForDeletedDish() {
        given()
                .when()
                .get("/api/dishes/{id}", createdDishId)
                .then()
                .statusCode(404);
    }
}
