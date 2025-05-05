package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing user roles in the system.
 */
public enum Role {
    LÆGE,
    SYGEPLEJERSKE,
    KOK,
    HOVEDKOK,
    KØKKENPERSONALE;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role fromString(String key) {
        if (key == null) return null;
        try {
            return Role.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ugyldig rolle: " + key);
        }
    }
}
