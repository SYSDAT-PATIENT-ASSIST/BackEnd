package dk.patientassist.control;

import dk.patientassist.persistence.enums.Role;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Patient Assist
 */
public class AuthController
{
    public static EndpointGroup getEndpoints()
    {
        return () -> {
            path("/auth", () -> {
                get("/login", AuthController::login, Role.GUEST);
                get("/register", AuthController::register, Role.GUEST);
                // AccessController accessController = new AccessController();
            });
        };
    }

    private static void register(@NotNull Context ctx)
    {
    }

    private static void login(@NotNull Context ctx)
    {
    }
}
