package dk.patientassist.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dk.patientassist.utilities.Utils;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.json.JavalinJackson;

/**
 * Patient Assist
 */
public class MasterController {

    private static final Logger logger = LoggerFactory.getLogger(MasterController.class);

    public static Javalin start(int port) {
        Javalin jav = setup();
        jav.start(port);
        return jav;
    }

    private static Javalin setup() {
        try {
            AuthController.init();
            AccessController.init();
        } catch (Exception e) {
            logger.error("Failed to initialize one or more controllers: {}", e.getMessage());
            System.exit(1);
        }

        @SuppressWarnings("deprecation")
        Javalin jav = Javalin.create(config -> {
            config.jsonMapper(
                    new JavalinJackson().updateMapper(mapper -> {
                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        mapper.registerModule(new JavaTimeModule());
                        mapper.writer(new DefaultPrettyPrinter());
                        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
                    }));
            config.showJavalinBanner = false;
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.requestLogger.http((ctx, ms) -> debugLog(ctx, ms));
            /* API */
            config.router.apiBuilder(AuthController.getEndpoints());
        });
        /* SECURITY */
        jav.beforeMatched(AccessController::check);
        /* CORS */
        jav.before(MasterController::corsHeaders);
        jav.options("/*", MasterController::corsHeadersOptions);
        /* EXCEPTIONS */
        jav.exception(HttpResponseException.class, MasterController::jsonErrorResponse);

        return jav;
    }

    private static void jsonErrorResponse(HttpResponseException e, Context ctx) {
        ctx.status(e.getStatus());
        ctx.json(Utils.JSONStatusObject(ctx));
    }

    private static void debugLog(Context ctx, float ms) {
        String logEntry = String.format(
                "%s: %s %s (%sms, statusCode: %d)",
                ctx.ip(),
                ctx.req().getMethod(),
                ctx.path(),
                ms,
                ctx.statusCode());
        logger.info(logEntry);
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }
}
