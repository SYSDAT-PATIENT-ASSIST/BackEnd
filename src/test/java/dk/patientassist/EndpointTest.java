package dk.patientassist;

import dk.patientassist.config.ApplicationConfig;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class EndpointTest {

    private static final String BASE_URI = "http://localhost:7070/api";

    private static Javalin app;
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeAll
    public static void setup() {
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        app = ApplicationConfig.startServer(7070);
        RestAssured.baseURI = BASE_URI;

        entityManagerFactory = HibernateConfig.getEntityManagerFactory();
    }

    @BeforeEach
    public void setupEach() {
        // Initialiser EntityManager og begynder transaktionen
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        // Opretter rollen til testbrugeren
        Role userRole = new Role("user");

        // Gemmer rollen i databasen
        entityManager.persist(userRole);

        // Opretter brugeren og tilknytter rollen
        User user = new User("kitchenstaff", "test123");
        user.addRole(userRole);

        // Gem brugeren i databasen
        entityManager.persist(user);

        // Commit transaktionen
        entityManager.getTransaction().commit();
    }

    @AfterEach
    public void tearDownEach() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            entityManager.createQuery("DELETE FROM Role").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @AfterAll
    public static void tearDown() {
        ApplicationConfig.stopServer(app);
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Test
    public void testLogin() {
        // Test login med korrekt brugernavn og adgangskode
        Response response = given()
                .contentType("application/json")
                .body("{ \"username\": \"kitchenstaff\", \"password\": \"test123\" }")
                .when()
                .post("/auth/login/")
                .then()
                .statusCode(200) // Tjek at statuskode er 200
                .body("token", notNullValue()) // Bekræft at token er returneret
                .body("username", equalTo("kitchenstaff")) // Bekræft at brugernavnet er korrekt
                .extract()
                .response();
    }

}
