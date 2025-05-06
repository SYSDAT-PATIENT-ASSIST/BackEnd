package dk.patientassist.security.controllers;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Arrays;
import java.util.Set;

public class AccessController implements IAccessController {

    private final SecurityController securityController = SecurityController.getInstance();

    /**
     * Used by Javalin to check if the authenticated user has access to the route.
     */
    public void accessHandler(Context ctx) {
        // Public route
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.ANYONE)) return;

        // Authenticate
        try {
            securityController.authenticate().handle(ctx);
        } catch (UnauthorizedResponse e) {
            throw new UnauthorizedResponse(e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid or missing token");
        }

        // Authorize
        UserDTO user = ctx.attribute("user");
        Set<RouteRole> allowedRoles = ctx.routeRoles();
        if (!securityController.authorize(user, allowedRoles)) {
            throw new UnauthorizedResponse("Unauthorized. You have roles: " + user.getRoles() +
                    ". Required: " + allowedRoles);
        }
    }

    /**
     * Returns true if the user has the specified role.
     */
    public boolean hasRole(Context ctx, Role role) {
        UserDTO user = ctx.attribute("user");
        return user != null && user.getRoles().contains(role.toString());
    }

    /**
     * Throws if the user does not have the required role.
     */
    public void requireRole(Context ctx, Role role) {
        if (!hasRole(ctx, role)) {
            throw new UnauthorizedResponse("Access denied. Required role: " + role);
        }
    }

    /**
     * Throws if the user does not have at least one of the required roles.
     */
    public void requireOneOfRoles(Context ctx, Role... roles) {
        UserDTO user = ctx.attribute("user");
        if (user == null) throw new UnauthorizedResponse("You must be logged in");

        for (Role role : roles) {
            if (user.getRoles().contains(role.toString())) return;
        }

        throw new UnauthorizedResponse("Access denied. Required one of: " + Arrays.toString(roles));
    }
}
