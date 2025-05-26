package dk.patientassist.config;

import dk.patientassist.routes.DishRoutes;
import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.routes.SecurityRoutes;
import dk.patientassist.security.enums.Role;
import dk.patientassist.security.controllers.AccessController;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static final AccessController accessController = new AccessController();
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void configuration(JavalinConfig cfg) {
        cfg.showJavalinBanner = false;

        cfg.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        cfg.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
        cfg.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        cfg.router.apiBuilder(DishRoutes.getDishRoutes());
    }

    public static Javalin startServer(int port) {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        app.beforeMatched(accessController::accessHandler);
        app.before(ApplicationConfig::cors);
        app.options("/*", ApplicationConfig::corsOptions);

        app.exception(ApiException.class, ApplicationConfig::handleApiException);
        app.exception(Exception.class, ApplicationConfig::handleGeneralException);

        app.get("/api/health", ctx -> ctx.result("OK"));

        app.start(port);
        return app;
    }

    private static void handleApiException(ApiException e, Context ctx) {
        ctx.status(e.getCode()).json(Utils.JSONStatusObject(ctx, e));
    }

    private static void handleGeneralException(Exception e, Context ctx) {
        log.error("Unhandled exception", e);
        ctx.status(500).json(Utils.JSONStatusObject(ctx, e));
    }

    private static void cors(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "http://localhost:5173")
                .header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type,Authorization")
                .header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsOptions(Context ctx) {
        cors(ctx);
        ctx.status(204);
    }
}
