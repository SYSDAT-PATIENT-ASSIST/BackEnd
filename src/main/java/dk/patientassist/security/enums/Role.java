package dk.patientassist.security.enums;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, KITCHEN_STAFF, HEAD_CHEF, ADMIN;
}
