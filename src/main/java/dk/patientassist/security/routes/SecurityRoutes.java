package dk.patientassist.security.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.utilities.Utils;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;

public class SecurityRoutes {
    private static ObjectMapper jsonMapper = Utils.getObjectMapperCompact();
    private static SecurityController securityController = SecurityController.getInstance();

    public static EndpointGroup getSecurityRoutes() {
        return () -> {
            path("/auth", () -> {
                get("/healthcheck", securityController::healthCheck, Role.ANYONE);
                get("/test", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from Open Deployment")),
                        Role.ANYONE);
                post("/login", securityController.login(), Role.ANYONE);
                post("/register", securityController.register(), Role.ANYONE);
                post("/user/addrole", securityController.addRole(), Role.ANYONE);
            });
        };
    }

    public static EndpointGroup getSecuredRoutes() {
        return () -> {
            path("/protected", () -> {
                get("/user_demo",
                        (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")),
                        Role.ADMIN);
                get("/admin_demo",
                        (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")),
                        Role.ADMIN);
            });
        };
    }
}
