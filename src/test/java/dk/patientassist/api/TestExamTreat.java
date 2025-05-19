package dk.patientassist.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import dk.patientassist.api.resources.EmployeeData;
import dk.patientassist.api.resources.EventData;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.persistence.ent.ExamTreat;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.service.dto.ExamTreatDTO;
import dk.patientassist.service.dto.ExamTreatTypeDTO;
import dk.patientassist.utilities.ExamTreatPopulator;
import dk.patientassist.utilities.MockData;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Event API tests
 */
public class TestExamTreat {

    static EntityManagerFactory emf;
    static Javalin jav;
    static ObjectMapper jsonMapper;
    static String jwt;
    static EmployeeData empData;
    static EventData eventData;
    static int port;

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    static Faker faker = new Faker();
    static Random rng = new Random();

    static ExamTreatCategoryDTO[] ETCatsOnDisk;

    @BeforeAll
    static void setup() {
        try {
            jsonMapper = Utils.getObjectMapperCompact();
            jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
            jwtIss = Utils.getConfigProperty("JWT_ISSUER");
            jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
            jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));

            String ETCatsStr = new String(TestExamTreat.class.getClassLoader()
                    .getResourceAsStream("data/exams_and_treatments_data.json").readAllBytes());
            ETCatsOnDisk = Utils.getObjectMapperCompact().readValue(ETCatsStr, ExamTreatCategoryDTO[].class);
        } catch (Exception e) {
            Assertions.fail("setup failed: " + e.getMessage());
        }

        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();

        port = 9999;
        jav = MasterController.start(Mode.TEST, port);

        empData = new EmployeeData();
        register(empData.guest, "guest");
        register(empData.admin, "admin");

        try {
            ExamTreatPopulator.load("data/exams_and_treatments_data.json");
        } catch (Exception e) {
            Assertions.fail("setup failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void teardown() {
        jav.stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        logout();
    }

    /* TESTS */

    @Test
    void readCategories() {
        login(empData.guest, "guest");
        try {
            String response = get("examinations-and-treatments/categories", 200);

            ExamTreatCategoryDTO[] ETCatsInResponse = Utils.getObjectMapperCompact()
                    .readValue(response, ExamTreatCategoryDTO[].class);

            Assertions.assertEquals(ETCatsInResponse.length, ETCatsOnDisk.length,
                    "ET data should have same size on disk and in API");

            for (var ETCat : ETCatsOnDisk) {
                boolean found = false;
                for (var ETCatInResponse : ETCatsInResponse) {
                    if (ETCat.name.equals(ETCatInResponse.name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Assertions.fail("Examinations and treatment data on disk does not match api response");
                }
            }

        } catch (Exception e) {
            Assertions.fail("Examinations and treatment data on disk does not match api response: " + e.getMessage());
        }
    }

    @Test
    void readSubCategory() {
        try {
            for (var ETCatOnDisk : ETCatsOnDisk) {
                String response = get("examinations-and-treatments/categories/" + ETCatOnDisk.urlSafeName, 200);
                ExamTreatTypeDTO[] ETTypesInResponse = Utils.getObjectMapperCompact()
                        .readValue(response, ExamTreatTypeDTO[].class);

                Assertions.assertEquals(ETTypesInResponse.length, ETCatOnDisk.examTreatTypes.length,
                        "ET category's subcategories should match what is on disk");

                Arrays.sort(ETCatOnDisk.examTreatTypes);
                Arrays.sort(ETTypesInResponse);
                for (int i = 0; i < ETTypesInResponse.length; i++) {
                    Assertions.assertEquals(ETTypesInResponse[i].name, ETCatOnDisk.examTreatTypes[i].name,
                            "ET category's subcategories should match what is on disk : "
                                    + ETTypesInResponse[i].name + " vs. " + ETCatOnDisk.examTreatTypes[i].name);
                    Assertions.assertEquals(ETTypesInResponse[i].urlSafeName, ETCatOnDisk.examTreatTypes[i].urlSafeName,
                            "ET category's subcategories should match what is on disk"
                                    + ETTypesInResponse[i].urlSafeName + " vs. "
                                    + ETCatOnDisk.examTreatTypes[i].urlSafeName);
                }

            }
        } catch (Exception e) {
            Assertions.fail("something went wrong: " + e.getMessage());
        }
    }

    @Test
    void readArticle() {
        try {
            for (var ETCatOnDisk : ETCatsOnDisk) {
                for (var ETSubCatOnDisk : ETCatOnDisk.examTreatTypes) {
                    for (var ETArticleOnDisk : ETSubCatOnDisk.examTreats) {
                        String response = get("examinations-and-treatments/articles/" + ETArticleOnDisk.urlSafeName,
                                200);
                        ExamTreatDTO ETArticleInResponse = Utils.getObjectMapperCompact()
                                .readValue(response, ExamTreatDTO.class);
                        Assertions.assertEquals(ETArticleInResponse.name, ETArticleOnDisk.name);
                        Assertions.assertEquals(ETArticleInResponse.urlSafeName, ETArticleOnDisk.urlSafeName);
                    }
                }
            }

        } catch (Exception e) {
            Assertions.fail("articles on disk should match API response");
        }
    }

    /* HELPER METHODS */
    static String get(String endpoint, int expStatus) {
        return RestAssured.given().port(port)
                .header("Authorization", "Bearer " + jwt)
                .when().get("/api/" + endpoint)
                .then().assertThat().statusCode(expStatus)
                .and().extract().body().asString();
    }

    static void register(EmployeeDTO empDetails, String pw) {
        try {
            jwt = RestAssured.given().port(port).contentType("application/json")
                    .body(empDetails.makeRegistrationForm(pw))
                    .when().post("/api/auth/register")
                    .then().statusCode(201)
                    .and().extract().path("token");
        } catch (Exception e) {
            Assertions.fail("registration error");
        }
    }

    static void login(EmployeeDTO empDetails, String pw) {
        try {
            jwt = RestAssured.given().port(port).contentType("application/json").body(empDetails.makeLoginForm(pw))
                    .when().post("/api/auth/login")
                    .then().statusCode(200)
                    .and().extract().path("token");
        } catch (Exception e) {
            Assertions.fail("login error");
        }
    }

    static void logout() {
        jwt = "";
    }
}
