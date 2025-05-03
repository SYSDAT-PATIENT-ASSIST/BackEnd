package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing dish statuses in the system.
 */
public enum DishStatus {
    AVAILABLE,
    UNAVAILABLE,
    SOLD_OUT;

    /**
     * Allows case-insensitive deserialization of DishStatus enum values from JSON.
     * For example, both "available" and "AVAILABLE" will be mapped to DishStatus.AVAILABLE.
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
