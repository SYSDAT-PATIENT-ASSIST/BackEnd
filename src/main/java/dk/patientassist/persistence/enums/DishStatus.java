package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the availability status of a dish.
 */
public enum DishStatus {
    TILGÆNGELIG, // AVAILABLE
    UDGÅET, // DISCONTINUED
    UDSOLGT; // SOLD_OUT

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static DishStatus fromString(String key) {
        if (key == null)
            return null;
        try {
            return DishStatus.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ugyldig dish status: " + key);
        }
    }
}
