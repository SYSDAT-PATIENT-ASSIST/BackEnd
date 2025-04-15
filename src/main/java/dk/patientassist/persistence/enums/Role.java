package dk.patientassist.persistence.enums;

import io.javalin.security.RouteRole;

/**
 * Patient Assist
 */
public enum Role implements RouteRole
{
    GUEST, DOCTOR, NURSE, CHEF, HEADCHEF, ADMIN;
}
