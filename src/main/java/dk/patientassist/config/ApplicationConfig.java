package dk.patientassist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.patientassist.routes.DishRoutes;
import dk.patientassist.security.controllers.AccessController;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.security.enums.Role;
import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.routes.SecurityRoutes;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static final ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static final SecurityController securityController = SecurityController.getInstance();
    private static final AccessController accessController = new AccessController();
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        config.router.contextPath = "";

        //API routes
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        config.router.apiBuilder(DishRoutes.getDishRoutes());

    }

    public static Javalin startServer(int port) {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        app.beforeMatched(accessController::accessHandler);
        app.before(ApplicationConfig::corsHeaders);
        app.options("/*", ApplicationConfig::corsHeadersOptions);

        app.exception(Exception.class, ApplicationConfig::generalExceptionHandler);
        app.exception(ApiException.class, ApplicationConfig::apiExceptionHandler);

        app.start(port);
        return app;
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }

    private static void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("Unhandled exception", e);
        ctx.status(500).json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
    }

    public static void apiExceptionHandler(ApiException e, Context ctx) {
        ctx.status(e.getCode());
        logger.warn("API exception: {}", e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        corsHeaders(ctx);
        ctx.status(204);
    }
}
