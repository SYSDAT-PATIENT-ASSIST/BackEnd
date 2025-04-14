package dk.patientassist.utilities;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.exceptions.ApiException;
import io.javalin.http.Context;

/**
 *
 * Patient Assist
 *
 */
public class Utils
{
    static Logger logger = LoggerFactory.getLogger(Utils.class);
    static Properties config_properties;
    static DateTimeFormatter dateTimeFormatter;
    static ObjectMapper objectMapperDef = new ObjectMapper();
    static ObjectMapper objectMapper;

    public static String getPropertyValue(String key)
    {
        if (config_properties == null) {
            try {
                loadConfig("config.properties");
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.exit(1);
            }
        }

        String value = (String) config_properties.get(key);

        if (value == null) {
            logger.warn(String.format("Property %s not found in %s", key, "config_properties"));
        }

        return value;
    }

    public static ObjectMapper getObjectMapper()
    {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties in JSON
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.writer(new DefaultPrettyPrinter());
        }
        return objectMapper;
    }

    public static String JSONStatusMessage(Context ctx)
    {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("message", ctx.status().toString());
        msgMap.put("status", String.valueOf(ctx.statusCode()));
        msgMap.put("timestamp", dateTimeFormat(LocalDateTime.now()));
        try {
            return objectMapperDef.writeValueAsString(msgMap);
        } catch (Exception e) {
            return "{\"error\": \"Could not convert message to JSON\"}";
        }
    }

    public static String dateTimeFormat(LocalDateTime ldt)
    {
        if (dateTimeFormatter == null) {
            dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MILLI_OF_SECOND, 2, 3, true)
                .toFormatter();
        }
        return dateTimeFormatter.format(ldt);
    }

    public static double roundFloat(double num, int places)
    {
        BigDecimal val = BigDecimal.valueOf(num);
        val = val.setScale(places, RoundingMode.HALF_UP);
        return val.doubleValue();
    }

    private static void loadConfig(String name) throws IOException
    {
        config_properties = new Properties();
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(name)) {
            config_properties.load(is);
        } catch (IOException ex) {
            throw new IOException(String.format("Could not read property file %s: %s", name, ex.getMessage()));
        }
    }
}
