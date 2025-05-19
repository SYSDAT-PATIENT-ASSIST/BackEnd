package dk.patientassist.api;

import static dk.patientassist.api.impl.HelperMethods.fetchExamTreatArticles;
import static dk.patientassist.api.impl.HelperMethods.fetchExamTreatCategories;
import static dk.patientassist.api.impl.HelperMethods.fetchExamTreatSubcategories;
import static dk.patientassist.api.impl.HelperMethods.logout;
import static dk.patientassist.api.impl.HelperMethods.matchArticles;
import static dk.patientassist.api.impl.HelperMethods.matchCategories;
import static dk.patientassist.api.impl.HelperMethods.matchSubCategories;
import static dk.patientassist.api.impl.HelperMethods.setup;
import static dk.patientassist.api.impl.HelperMethods.stop;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.utilities.Utils;

/**
 * Event API tests
 */
public class TestExamTreat {

    static ExamTreatCategoryDTO[] ETCatsOnDisk;

    @BeforeAll
    static void init() {
        setup();
        loadETData();
    }

    @AfterAll
    static void teardown() {
        stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        logout();
    }

    /* TESTS */

    @Test
    void readCategories() {
        try {
            var ETCatsInResponse = fetchExamTreatCategories();
            matchCategories(ETCatsInResponse, ETCatsOnDisk);
        } catch (Exception e) {
            Assertions.fail("Examinations and treatment data on disk does not match api response: " + e.getMessage());
        }
    }

    @Test
    void readSubCategory() {
        try {
            for (var ETCatOnDisk : ETCatsOnDisk) {
                var ETTypesInResponse = fetchExamTreatSubcategories(ETCatOnDisk.urlSafeName);
                matchSubCategories(ETCatOnDisk.examTreatTypes, ETTypesInResponse);
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
                        var ETArticleInResponse = fetchExamTreatArticles(ETArticleOnDisk.urlSafeName);
                        matchArticles(ETArticleInResponse, ETArticleOnDisk);
                    }
                }
            }

        } catch (Exception e) {
            Assertions.fail("articles on disk should match API response");
        }
    }

    static void loadETData() {
        try {
            String ETCatsStr = new String(TestExamTreat.class.getClassLoader()
                    .getResourceAsStream("data/exams_and_treatments_data.json").readAllBytes());
            ETCatsOnDisk = Utils.getObjectMapperCompact().readValue(ETCatsStr, ExamTreatCategoryDTO[].class);
        } catch (Exception e) {
            Assertions.fail("setup failed: " + e.getMessage());
        }
    }

}
