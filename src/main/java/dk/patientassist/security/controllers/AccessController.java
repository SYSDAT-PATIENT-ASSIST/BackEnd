package dk.patientassist.security.controllers;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Arrays;
import java.util.Set;


/**
 * Enforces authentication and authorization on all routes using beforeMatched().
 */
public class AccessController {

    private final ISecurityController securityController;

    /**
     * Default constructor for production: uses the real SecurityController.
     */
    public AccessController() {
        this(new SecurityController());
    }

    /**
     * Constructor for tests: allows injection of a mock ISecurityController.
     */
    public AccessController(ISecurityController securityController) {
        this.securityController = securityController;
    }

    /**
     * This is called on every matched route to enforce access control.
     */
    public void accessHandler(Context ctx) {
        Set<RouteRole> roles = ctx.routeRoles();

        // 1) Allow public routes (ANYONE or empty)
        if (roles.isEmpty() || roles.contains(Role.ANYONE)) {
            return;
        }

        // 2) Authenticate user
        try {
            securityController.authenticate().handle(ctx);
        } catch (UnauthorizedResponse e) {
            throw new UnauthorizedResponse(e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid or missing token");
        }

        // 3) Authorize user
        UserDTO user = ctx.attribute("user");
        boolean allowed = securityController.authorize(user, roles);

        if (!allowed) {
            throw new UnauthorizedResponse(
                    "Unauthorized. You have roles: " + user.getRoles() +
                            ". Required: " + roles
            );
        }
    }

    // --- Optional helpers ---

    public boolean hasRole(Context ctx, Role role) {
        UserDTO user = ctx.attribute("user");
        return user != null && user.getRoles().contains(role.toString());
    }

    public void requireRole(Context ctx, Role role) {
        if (!hasRole(ctx, role)) {
            throw new UnauthorizedResponse("Access denied. Required role: " + role);
        }
    }

    public void requireOneOfRoles(Context ctx, Role... roles) {
        UserDTO user = ctx.attribute("user");
        if (user == null) {
            throw new UnauthorizedResponse("You must be logged in");
        }
        for (Role role : roles) {
            if (user.getRoles().contains(role.toString())) {
                return;
            }
        }
        throw new UnauthorizedResponse("Access denied. Required one of: " + Arrays.toString(roles));
    }
}
