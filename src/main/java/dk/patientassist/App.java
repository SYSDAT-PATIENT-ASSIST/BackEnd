package dk.patientassist;

import static dk.patientassist.config.Mode.DEV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.utilities.EmployeePopulator;
import dk.patientassist.utilities.EventPopulator;
import dk.patientassist.utilities.ExamTreatPopulator;

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
            ExamTreatPopulator.load("data/exam_treatment_data.json");

        } catch (Exception e) {

            HibernateConfig.getEntityManagerFactory().close();
            logger.error("Error initializing application: {}{}", e.getMessage(),
                    System.lineSeparator());
            e.printStackTrace();

        }
    }
}
