package dk.patientassist;

import dk.patientassist.config.ApplicationConfig;
import dk.patientassist.persistence.HibernateConfig;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Init Hibernate
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
        LOGGER.info("Hibernate initialized in DEV mode");

        // Start Javalin server with pre-configured routes
        Javalin app = ApplicationConfig.startServer(7070);

        LOGGER.info("Server running at http://localhost:7070");
    }
}
