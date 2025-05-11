package dk.patientassist.security.enums;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE,
    KOK,
    HOVEDKOK,
    KØKKENPERSONALE,
    LÆGE,
    SYGEPLEJERSKE,
    ADMIN;
}