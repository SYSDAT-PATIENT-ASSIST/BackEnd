package dk.patientassist.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.config.ServerConfig;
import io.javalin.Javalin;

/**
 * Patient Assist
 */
public class MasterController {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(MasterController.class);

    public static Javalin start(int port) {
        Javalin jav = ServerConfig.setup();
        jav.start(port);
        return jav;
    }
}
