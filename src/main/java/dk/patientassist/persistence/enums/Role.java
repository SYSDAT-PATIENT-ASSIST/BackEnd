package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing user roles in the system.
 */
public enum Role {
    DOCTOR,
    NURSE,
    CHEF,
    HEADCHEF,
    KITCHENS_STAFF;

    /**
     * Allows case-insensitive deserialization of Role enum values from JSON.
     * For example, both "chef" and "CHEF" will be mapped to Role.CHEF.
     *
     * @param key the input string from JSON
     * @return the corresponding Role enum value, or null if input is null
     * @throws IllegalArgumentException if the key does not match any enum value
     */
    @JsonCreator
    public static Role fromString(String key) {
        return key == null ? null : Role.valueOf(key.toUpperCase());
    }
}
