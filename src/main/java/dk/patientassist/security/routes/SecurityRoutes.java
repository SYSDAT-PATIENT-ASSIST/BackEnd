package dk.patientassist.security.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.utilities.Utils;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Defines all /auth and /protected endpoints for the security API.
 */
public class SecurityRoutes {

    private static final ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static final SecurityController securityController = new SecurityController();

    /**
     * Public (unsecured) routes under /auth
     */
    public static EndpointGroup getSecurityRoutes() {
        return () -> {
            path("/auth", () -> {
                get("/healthcheck",
                        securityController::healthCheck,
                        Role.ANYONE
                );
                get("/test",
                        ctx -> ctx.json(jsonMapper.createObjectNode().put("msg","Hello from Open Deployment")),
                        Role.ANYONE
                );
                post("/login",
                        securityController.login(),
                        Role.ANYONE
                );
                post("/register",
                        securityController.register(),
                        Role.ANYONE
                );
            });
        };
    }

    /**
     * Secured routes under /protected (requires ADMIN)
     */
    public static EndpointGroup getSecuredRoutes() {
        return () -> {
            path("/protected", () -> {
                get("/user_demo",
                        ctx -> ctx.json(jsonMapper.createObjectNode().put("msg","Hello from USER Protected")),
                        Role.ADMIN
                );
                get("/admin_demo",
                        ctx -> ctx.json(jsonMapper.createObjectNode().put("msg","Hello from ADMIN Protected")),
                        Role.ADMIN
                );
                post("/user/addrole",
                        securityController.addRole(),
                        Role.ADMIN
                );
            });
        };
    }
}