package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enum representing the status of an order in the system.
 */
public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED;

    /**
     * Allows case-insensitive deserialization of OrderStatus enum values from JSON.
     * For example, both "pending" and "PENDING" will be mapped to OrderStatus.PENDING.
     *
     * @param key the input string from JSON
     * @return the corresponding OrderStatus enum value, or null if input is null
     * @throws IllegalArgumentException if the key does not match any enum value
     */
    @JsonCreator
    public static OrderStatus fromString(String key) {
        return key == null ? null : OrderStatus.valueOf(key.toUpperCase());
    }
}
