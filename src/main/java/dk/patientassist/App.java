package dk.patientassist;

import static dk.patientassist.config.Mode.DEV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.persistence.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

/**
 * Patient Assist
 */
public class App
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	@SuppressWarnings("unused")
	private static EntityManagerFactory EMF;

    public static void main(String[] args)
    {
        HibernateConfig.Init(DEV);
        EMF = HibernateConfig.getEntityManagerFactory();
    }
}
