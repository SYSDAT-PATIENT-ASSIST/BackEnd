package dk.patientassist.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.patientassist.control.AccessController;
import dk.patientassist.control.AuthController;
import dk.patientassist.control.EventController;
import dk.patientassist.control.ExamTreatController;
import dk.patientassist.routes.DishRoutes;
import dk.patientassist.routes.TestEndpoints;
import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterConfig {

    private static final Logger logger = LoggerFactory.getLogger(RouterConfig.class);

    public static Javalin setup(Mode mode) {
        try {
            AuthController.init();
            AccessController.init();
        } catch (Exception e) {
            logger.error("Failed to initialize one or more controllers: {}", e.getMessage());
            System.exit(1);
        }

        Javalin jav = Javalin.create(config -> {
            config.jsonMapper(
                    new JavalinJackson().updateMapper(mapper -> {
                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        mapper.registerModule(new JavaTimeModule());
                        if (mode == Mode.DEV || mode == Mode.TEST) {
                            mapper.writer(new DefaultPrettyPrinter());
                        }
                        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    }));
            config.showJavalinBanner = false;
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
            /* DEV & TEST ONLY */
            if (mode == Mode.DEV || mode == Mode.TEST) {
                config.requestLogger.http((ctx, ms) -> debugLog(ctx, ms));
                config.router.apiBuilder(TestEndpoints.getEndpoints());
            }
            /* API */
            config.router.apiBuilder(AuthController.getEndpoints());
            config.router.apiBuilder(EventController.getEndpoints());
            config.router.apiBuilder(DishRoutes.getDishRoutes());
            config.router.apiBuilder(ExamTreatController.getEndpoints());
        });
        /* SECURITY */
        jav.beforeMatched(AccessController::check);
        /* CORS */
        jav.before(RouterConfig::corsHeaders);
        jav.options("/*", RouterConfig::corsHeadersOptions);
        /* EXCEPTIONS */
        jav.exception(HttpResponseException.class, RouterConfig::jsonErrorResponse);

        return jav;
    }

    private static void jsonErrorResponse(HttpResponseException e, Context ctx) {
        ctx.status(e.getStatus());
        ctx.json(Utils.JSONStatusObject(ctx, e));
    }

    private static void debugLog(Context ctx, float ms) {
        String logEntry = String.format("%s: %s %s (%sms, statusCode: %d)",
                ctx.ip(), ctx.req().getMethod(), ctx.path(), ms, ctx.statusCode());
        logger.info(logEntry);
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }
}
