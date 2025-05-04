package dk.patientassist.control;

import static io.javalin.apibuilder.ApiBuilder.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.apibuilder.EndpointGroup;

/**
 * Patient Assist
 */
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    public static EndpointGroup getEndpoints() {
        return () -> {
            path("/events", () -> {
                // post("/login", AuthController::login, Role.GUEST);
                // post("/register", AuthController::register, Role.GUEST);
            });
        };
    }
}
