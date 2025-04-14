package dk.patientassist;

import dk.patientassist.persistence.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Patient Assist
 *
 */
public class App
{
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static EntityManagerFactory EMF;

    public static void main(String[] args)
    {
        logger.debug("lol");
        logger.info("lol");
        logger.warn("lol");
        logger.error("lol");
        // HibernateConfig.Init(HibernateConfig.Mode.DEV);
        // EMF = HibernateConfig.getEntityManagerFactory();
    }
}
