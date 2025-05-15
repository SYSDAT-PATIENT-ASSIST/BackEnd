package dk.patientassist.security.controllers;

import io.javalin.http.Context;

public interface IAccessController {
    void accessHandler(Context ctx);
}
