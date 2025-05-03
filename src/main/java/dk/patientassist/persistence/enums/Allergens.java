package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing common food allergens that may be present in dishes.
 */
public enum Allergens {
    GLUTEN,
    LACTOSE,
    NUTS,
    EGGS,
    FISH,
    SHELLFISH,
    SOY,
    WHEAT,
    SESAME,
    MUSTARD,
    CELERY,
    SULPHITES,
    LUPIN;

    /**
     * Allows case-insensitive deserialization of Allergens enum values from JSON.
     * For example, both "nuts" and "NUTS" will be mapped to Allergens.NUTS.
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

/*
    EGGS – Found in baked goods, pasta, sauces, etc.
    FISH – Common in many cuisines.
    SHELLFISH – Includes crustaceans like shrimp, crab, lobster.
    SOY – Present in many processed foods and sauces.
    WHEAT – Separate from gluten for clarity; wheat allergies are distinct from gluten intolerance.
    SESAME – Increasingly common allergen.
    MUSTARD – Recognized in EU allergen lists.
    CELERY
 */