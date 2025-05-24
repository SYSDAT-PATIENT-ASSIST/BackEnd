package dk.patientassist.security.enums;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE,
    CHEF,
    HEAD_CHEF,
    KITCHEN_STAFF,
    DOCTOR,
    NURSE,
    ADMIN;
}