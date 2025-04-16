package dk.patientassist.control;

import dk.patientassist.persistence.enums.Role;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patient Assist
 */
public class AccessController
{
    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AccessController.class);

    public static void check(@NotNull Context ctx) {
        logger.debug("Checking access for route {}", ctx.path());
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.GUEST)) {
            return;
        }
    }
}
