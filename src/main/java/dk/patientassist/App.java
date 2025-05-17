package dk.patientassist;

import static dk.patientassist.config.Mode.DEV;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.utilities.EmployeePopulator;
import dk.patientassist.utilities.EventPopulator;
import dk.patientassist.utilities.ExamTreatPopulator;
import dk.patientassist.utilities.Utils;
import dk.patientassist.utilities.WebScraper;

/**
 * Patient Assist
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        HibernateConfig.init(DEV);

        try {

            MasterController.start(Mode.DEV, 9999);

            /* TEST DATA */

            EventPopulator.populate(250);
            EmployeePopulator.addAdmin();
            ExamTreatPopulator.load("data/exams_and_treatments_data.json");

        } catch (Exception e) {

            HibernateConfig.getEntityManagerFactory().close();
            logger.error("Error initializing application: {}{}", e.getMessage(),
                    System.lineSeparator());
            e.printStackTrace();

        }
    }
}
