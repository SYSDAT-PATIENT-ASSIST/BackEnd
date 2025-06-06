package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing common food allergens that may be present in dishes.
 */
public enum Allergens {
    GLUTEN,
    LAKTOSE,
    LACTOSE,
    NØDDER,
    NUTS,
    ÆG,
    EGG,
    FISK,
    FISH,
    SKALDYR,
    SHELLFISH,
    SOJA,
    SOY,
    MÆLK,
    MILK,
    HVEDE,
    SESAM,
    SENNEP,
    SELLERI,
    SULFITTER,
    LUPIN;

    /**
     * Serialize enum as lowercase string.
     * 
     * @return lowercase string value of enum
     */
    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    /**
     * Deserialize enum from case-insensitive string.
     * 
     * @param key input string
     * @return matching enum or throws exception
     */
    @JsonCreator
    public static Allergens fromString(String key) {
        if (key == null)
            return null;
        key = key.trim().toLowerCase();
        for (Allergens allergen : values()) {
            if (allergen.toValue().equals(key)) {
                return allergen;
            }
        }
        throw new IllegalArgumentException("Ugyldig allergen: " + key);
    }
}
