package dk.patientassist;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.utilities.EmployeePopulator;
import dk.patientassist.utilities.EventPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dk.patientassist.config.Mode.DEV;

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

            EmployeeDTO guest = new EmployeeDTO();
            guest.email = "guest@email.dk";
            guest.firstName = "guest";
            guest.middleName = "guest";
            guest.lastName = "guest";
            guest.roles = new Role[] { Role.GUEST };
            guest.sections = new Long[0];
            guest.setPassword("guest");
            System.out.println(guest.makeLoginForm("guest"));
            System.out.println(guest.makeRegistrationForm("guest"));

        } catch (Exception e) {

            HibernateConfig.getEntityManagerFactory().close();
            logger.error("Error initializing application: {}{}", e.getMessage(), System.lineSeparator());

        }
    }
}
