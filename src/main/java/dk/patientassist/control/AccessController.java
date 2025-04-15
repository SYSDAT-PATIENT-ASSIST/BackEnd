package dk.patientassist.control;

import dk.patientassist.persistence.enums.Role;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

/**
 * Patient Assist
 */
public class AccessController
{
    public static void check(@NotNull Context ctx) {
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.GUEST)) {
            return;
        }
    }
}
