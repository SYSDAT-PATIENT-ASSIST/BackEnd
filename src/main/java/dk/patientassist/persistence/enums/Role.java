package dk.patientassist.persistence.enums;

import io.javalin.security.RouteRole;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Patient Assist
 */
public enum Role implements RouteRole
{
    GUEST,
    DOCTOR,
    NURSE,
    CHEF,
    HEADCHEF,
    KITCHEN_STAFF,
    ADMIN;

    public static String stringify(Role[] roles)
    {
        return String.join(",", Arrays.stream(roles).map(r -> r.name()).toList());
    }

    public static String stringify(List<Role> roles)
    {
        return String.join(",", roles.stream().map(r -> r.name()).toList());
    }

    public static boolean find(Role role, List<Role> roles)
    {
        return roles != null && roles.contains(role);
    }

    public static boolean find(Role role, Role[] roles)
    {
        return roles != null && Arrays.asList(roles).contains(role);
    }

    public static boolean find(Collection<RouteRole> roles_a, Role[] roles_b)
    {
        if (roles_a == null || roles_b == null) {
            return false;
        }
        for (Role role : roles_b) {
            if (roles_a.contains(role)) {
                return true;
            }
        }
        return true;
    }
}
