package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing the availability status of a dish.
 * Enum values are written in Danish for end-user readability.
 */
public enum DishStatus {
    TILGÆNGELIG, // AVAILABLE
    UDGÅET, // UNAVAILABLE
    UDSOLGT; // SOLD_OUT

    /**
     * Allows case-insensitive deserialization of DishStatus enum values from JSON.
     * For example, both "udsolgt" and "UDSOLGT" will be mapped to DishStatus.UDSOLGT.
     *
     * @param key the input string from JSON
     * @return the corresponding DishStatus enum value, or null if input is null
     * @throws IllegalArgumentException if the key does not match any enum value
     */
    @JsonCreator
    public static DishStatus fromString(String key) {
        return key == null ? null : DishStatus.valueOf(key.toUpperCase());
    }
}