package dk.patientassist.utilities;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Patient Assist
 */
public class Utils {
    static final Logger logger = LoggerFactory.getLogger(Utils.class);
    static Properties config_properties = new Properties();
    static DateTimeFormatter DTFormatterDefault;
    static ObjectMapper objectMapperCompact;
    static ObjectMapper objectMapperPretty;

    public static String getConfigProperty(String key) {
        if (config_properties.isEmpty()) {
            try {
                loadConfig();
            } catch (IOException e) {
                logger.error(e.getMessage());
                return null;
            }
        }

        String value = (String) config_properties.get(key);

        if (value == null) {
            logger.warn("property {} not found in project configuration", key);
        }

        return value;
    }

    public static ObjectMapper getObjectMapperPretty() {
        if (objectMapperPretty == null) {
            objectMapperPretty = new ObjectMapper();
            objectMapperPretty.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapperPretty.registerModule(new JavaTimeModule());
            objectMapperPretty.writer(new DefaultPrettyPrinter());
        }
        return objectMapperPretty;
    }

    public static ObjectMapper getObjectMapperCompact() {
        if (objectMapperCompact == null) {
            objectMapperCompact = new ObjectMapper();
            objectMapperCompact.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapperCompact.registerModule(new JavaTimeModule());
        }
        return objectMapperCompact;
    }

    public static ObjectNode JSONStatusObject(Context ctx, Exception e) {
        ObjectNode msg = Utils.getObjectMapperCompact().createObjectNode();
        msg.put("message", e.getMessage());
        msg.put("status", String.valueOf(ctx.statusCode()));
        msg.put("timestamp", dateTimeFormat(LocalDateTime.now()));
        return msg;
    }

    public static String JSONStatusMessage(Context ctx) {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("message", ctx.status().toString());
        msgMap.put("status", String.valueOf(ctx.statusCode()));
        msgMap.put("timestamp", dateTimeFormat(LocalDateTime.now()));
        try {
            return getObjectMapperCompact().writeValueAsString(msgMap);
        } catch (Exception e) {
            return "{\"error\": \"Could not convert message to JSON\"}";
        }
    }

    public static String dateTimeFormat(LocalDateTime ldt) {
        if (DTFormatterDefault == null) {
            DTFormatterDefault = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.MILLI_OF_SECOND, 2, 3, true)
                    .toFormatter();
        }
        return DTFormatterDefault.format(ldt);
    }

    public static double roundFloat(double num, int places) {
        BigDecimal val = BigDecimal.valueOf(num);
        val = val.setScale(places, RoundingMode.HALF_UP);
        return val.doubleValue();
    }

    private static void loadConfig() throws IOException {
        try (InputStream istream = Utils.class.getClassLoader().getResourceAsStream("config.properties")) {
            config_properties.load(istream);
            config_properties.forEach((key, value) -> { // environment variables trump project's config props
                if (System.getenv((String) key) != null) {
                    config_properties.put(key, System.getenv((String) key));
                }
            });
        }
    }
}
