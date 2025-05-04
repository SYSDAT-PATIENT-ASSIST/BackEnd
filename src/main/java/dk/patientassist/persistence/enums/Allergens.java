package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing common food allergens that may be present in dishes.
 */
public enum Allergens {
    GLUTEN,
    LAKTOSE,
    NØDDER,
    ÆG,
    FISK,
    SKALDYR,
    SOJA,
    HVEDE,
    SESAM,
    SENNEP,
    SELLERI,
    SULFITTER,
    LUPIN;

    /**
     * Allows case-insensitive deserialization of Allergens enum values from JSON.
     * For example, both "ÆG" and "ÆG" will be mapped to Allergens.ÆG.
     *
     * @param key the input string from JSON
     * @return the corresponding Allergens enum value, or null if input is null
     * @throws IllegalArgumentException if the key does not match any enum value
     */
    @JsonCreator
    public static Allergens fromString(String key) {
        return key == null ? null : Allergens.valueOf(key.toUpperCase());
    }
}