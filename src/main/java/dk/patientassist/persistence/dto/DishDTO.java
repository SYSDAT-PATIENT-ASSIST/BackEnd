package dk.patientassist.persistence.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a {@link Dish} entity.
 * Used for API input/output and internal DAO interactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DishDTO {

    /**
     * Unique identifier of the dish.
     */
    private Integer id;

    /**
     * Name of the dish.
     */
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 100)
    private String name;

    /**
     * Description of the dish.
     */
    @NotNull(message = "Description must not be null")
    @Size(max = 500)
    private String description;

    /**
     * The date from which the dish is available.
     */
    @NotNull(message = "Available from date must not be null")
    @JsonProperty("availableFrom")
    private LocalDate availableFrom;

    /**
     * The date until which the dish is available.
     */
    @NotNull(message = "Available until date must not be null")
    @JsonProperty("availableUntil")
    private LocalDate availableUntil;

    /**
     * Current status of the dish (e.g., TILGÆNGELIG, UDSOLGT).
     */
    @NotNull(message = "Status must not be null")
    private DishStatus status;

    /**
     * Energy content in kilocalories.
     */
    @PositiveOrZero(message = "kcal must be zero or positive")
    private double kcal;

    /**
     * Protein content in grams.
     */
    @PositiveOrZero(message = "Protein must be zero or positive")
    private double protein;

    /**
     * Carbohydrate content in grams.
     */
    @PositiveOrZero(message = "Carbohydrates must be zero or positive")
    private double carbohydrates;

    /**
     * Fat content in grams.
     */
    @PositiveOrZero(message = "Fat must be zero or positive")
    private double fat;

    /**
     * Allergen associated with the dish.
     */
    @NotNull(message = "Allergens must not be null")
    private Allergens allergens;

    /**
     * Validates that availableFrom is not after availableUntil.
     *
     * @return true if dates are in valid order or null
     */
    @AssertTrue(message = "Available from date must not be after available until date")
    public boolean isValidDateRange() {
        return availableFrom == null || availableUntil == null || !availableFrom.isAfter(availableUntil);
    }

    /**
     * Constructor without ID. Used when creating new dishes.
     */
    public DishDTO(String name, String description, LocalDate availableFrom, LocalDate availableUntil,
                   DishStatus status, double kcal, double protein, double carbohydrates, double fat, Allergens allergens) {
        this.name = name;
        this.description = description;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.status = status;
        this.kcal = kcal;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.allergens = allergens;
    }

    /**
     * Constructs a DTO from a {@link Dish} entity.
     *
     * @param dish the dish entity
     */
    public DishDTO(Dish dish) {
        this.id = dish.getId();
        this.name = dish.getName();
        this.description = dish.getDescription();
        this.availableFrom = dish.getAvailableFrom();
        this.availableUntil = dish.getAvailableUntil();
        this.status = dish.getStatus();
        this.kcal = dish.getKcal();
        this.protein = dish.getProtein();
        this.carbohydrates = dish.getCarbohydrates();
        this.fat = dish.getFat();
        this.allergens = dish.getAllergens();
    }

    /**
     * Copy constructor.
     *
     * @param other another DishDTO instance to copy data from
     */
    public DishDTO(DishDTO other) {
        this.id = other.getId();
        this.name = other.getName();
        this.description = other.getDescription();
        this.availableFrom = other.getAvailableFrom();
        this.availableUntil = other.getAvailableUntil();
        this.status = other.getStatus();
        this.kcal = other.getKcal();
        this.protein = other.getProtein();
        this.carbohydrates = other.getCarbohydrates();
        this.fat = other.getFat();
        this.allergens = other.getAllergens();
    }

}