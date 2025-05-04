package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing user roles in the system.
 * Enum values are written in Danish for localization support.
 */
public enum Role {
    LÆGE, //DOCTOR
    SYGEPLEJERSKE, //NURSE
    KOK, //CHEF
    HOVEDKOK, //HEADCHEF
    KØKKENPERSONALE; //KITCHEN_STAFF

    /**
     * Allows case-insensitive deserialization of Role enum values from JSON.
     * For example, both "kok" and "KOK" will be mapped to Role.KOK.
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