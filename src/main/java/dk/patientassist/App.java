package dk.patientassist;

import static dk.patientassist.config.Mode.DEV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.control.MasterController;
import dk.patientassist.persistence.HibernateConfig;

/**
 * Patient Assist
 */
public class App
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args)
    {
        HibernateConfig.init(DEV);

        try {

            MasterController.start(9999);

        } catch (Exception e) {

            HibernateConfig.getEntityManagerFactory().close();
            logger.error("Error initializing application: {}{}",
                e.getMessage(), System.lineSeparator());
            e.printStackTrace();

        }
    }
}
