package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the status of an order in the system.
 */
public enum OrderStatus {
    VENTER,      // PENDING
    FÆRDIG,      // COMPLETED
    ANNULLERET;  // CANCELLED

    /**
     * JSON output as lowercase string (e.g., "færdig").
     */
    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    /**
     * Case-insensitive deserialization from JSON.
     *
     * @param key input string (e.g., "færdig")
     * @return matching OrderStatus enum value
     * @throws IllegalArgumentException if the input does not match any enum
     */
    @JsonCreator
    public static OrderStatus fromString(String key) {
        if (key == null) return null;
        try {
            return OrderStatus.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ugyldig ordrestatus: " + key);
        }
    }
}
