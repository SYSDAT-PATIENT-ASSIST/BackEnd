package dk.patientassist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Central application configuration for Javalin server setup.
 * Handles route registration, security, exception handling, and CORS headers.
 */
public class ApplicationConfig {

    private static final ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static final SecurityController securityController = SecurityController.getInstance();
    private static final AccessController accessController = new AccessController();
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * Registers routers and security settings.
     */
    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        config.router.contextPath = "/api";

        // Register route groups
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        config.router.apiBuilder(SecurityRoutes.getDishRoutes());
    }

    /**
     * Starts the Javalin server with all configuration.
     *
     * @param port Port to bind the server to
     * @return Running Javalin app instance
     */
    public static Javalin startServer(int port) {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        // Global request filters
        app.beforeMatched(accessController::accessHandler);
        app.before(ApplicationConfig::corsHeaders);
        app.options("/*", ApplicationConfig::corsHeadersOptions);

        // Global exception handlers
        app.exception(Exception.class, ApplicationConfig::generalExceptionHandler);
        app.exception(ApiException.class, ApplicationConfig::apiExceptionHandler);

        app.start(port);
        return app;
    }

    /**
     * Stops the given Javalin server instance.
     */
    public static void stopServer(Javalin app) {
        app.stop();
    }

    /**
     * Handles generic uncaught exceptions.
     */
    private static void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("An unhandled exception occurred", e);
        ctx.json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
    }

    /**
     * Handles custom API exceptions.
     */
    public static void apiExceptionHandler(ApiException e, Context ctx) {
        ctx.status(e.getCode());
        logger.warn("An API exception occurred: Code: {}, Message: {}", e.getCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    /**
     * Applies CORS headers to all incoming requests.
     */
    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Preflight OPTIONS request handling for CORS.
     */
    private static void corsHeadersOptions(Context ctx) {
        corsHeaders(ctx);
        ctx.status(204);
    }
}
