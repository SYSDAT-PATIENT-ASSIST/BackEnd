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

import dk.patientassist.control.ApiException;
import io.javalin.http.Context;

public class Utils
{
    static ObjectMapper objectMapperDef = new ObjectMapper();
    static ObjectMapper objectMapper;

    public static String getPropertyValue(String propName, String resourceName)
    {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new ApiException(500, String.format("Property %s not found in %s", propName, resourceName));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ApiException(500, String.format("Could not read property %s. Did you remember to build the project with MAVEN?", propName));
        }
    }

    public static ObjectMapper getObjectMapper()
    {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties in JSON
            objectMapper.registerModule(new JavaTimeModule()); // Serialize and deserialize java.time objects
            objectMapper.writer(new DefaultPrettyPrinter());
        }
        return objectMapper;
    }

    public static String convertToJsonMessage(Context ctx, String property, String message)
    {
        Map<String, String> msgMap = new HashMap<>();
        //msgMap.put(property, message);  // Put the message in the map
        msgMap.put(property, ctx.status().toString());
        msgMap.put("status", String.valueOf(ctx.statusCode()));  // Put the status in the map
        msgMap.put("timestamp", new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MILLI_OF_SECOND, 2, 3, true)
                .toFormatter()
                .format(LocalDateTime.now()));
        try {
            return objectMapperDef.writeValueAsString(msgMap);  // Convert the map to JSON
        } catch (Exception e) {
            return "{\"error\": \"Could not convert  message to JSON\"}";
        }
    }

    public static DateTimeFormatter dateTimeFormatter()
    {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MILLI_OF_SECOND, 2, 3, true)
                .toFormatter();
    }

    public static double roundFloat(double num, int places)
    {
        BigDecimal val = BigDecimal.valueOf(num);
        val = val.setScale(places, RoundingMode.HALF_UP);
        return val.doubleValue();
    }
}
