package dk.patientassist.routes;

import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Patient Assist
 */
public class TestEndpoints {
    public static EndpointGroup getEndpoints() {
        return () -> {
            path("/auth", () -> {
                get("/guest", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from guest");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.GUEST);
                get("/admin_only", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from admin_only");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.ADMIN);
                get("/doctor_only", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from doctor_only");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.ADMIN, Role.DOCTOR);
                get("/chef_only", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from chef_only");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.ADMIN, Role.HEAD_CHEF, Role.CHEF);
                get("/headchef_only", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from headchef_only");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.ADMIN, Role.HEAD_CHEF);
                get("/nurse_only", ctx -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("message", "hello from nurse_only");
                    ctx.json(m);
                    ctx.status(200);
                }, Role.ADMIN, Role.DOCTOR, Role.NURSE);
            });
        };
    }
}
