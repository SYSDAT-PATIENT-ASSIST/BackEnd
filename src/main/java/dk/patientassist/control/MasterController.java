package dk.patientassist.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.config.Mode;
import dk.patientassist.config.RouterConfig;
import io.javalin.Javalin;

/**
 * Patient Assist
 */
public class MasterController {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(MasterController.class);

    public static Javalin start(Mode mode, int port) {
        Javalin jav = RouterConfig.setup(mode);
        jav.start(port);
        return jav;
    }
}
