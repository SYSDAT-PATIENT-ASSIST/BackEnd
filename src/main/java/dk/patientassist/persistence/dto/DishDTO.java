package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for transferring dish data between client and server.
 */
@Getter
@Setter
public class DishDTO {

    /**
     * Unique identifier for the dish.
     */
    private Integer id;

    /**
     * Name of the dish.
     */
    private String name;

    /**
     * Description of the dish.
     */
    private String description;

    /**
     * Start date for dish availability.
     */
    private LocalDate availableFrom;

    /**
     * End date for dish availability.
     */
    private LocalDate availableUntil;

    /**
     * Current status of the dish.
     */
    private DishStatus status;

    /**
     * Energy content (kcal).
     */
    private double kcal;

    /**
     * Protein content (g).
     */
    private double protein;

    /**
     * Carbohydrate content (g).
     */
    private double carbohydrates;

    /**
     * Fat content (g).
     */
    private double fat;

    /**
     * Set of allergens associated with the dish.
     */
    private Set<Allergens> allergens;

    /**
     * Single recipe associated with the dish.
     */
    private RecipeDTO recipe;

    /**
     * Full-args constructor.
     */
    public DishDTO(String name, String description, LocalDate availableFrom, LocalDate availableUntil,
                   DishStatus status, double kcal, double protein, double carbohydrates, double fat,
                   Set<Allergens> allergens, RecipeDTO recipe) {
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
        this.recipe = recipe;
    }

    /**
     * Constructs a DTO from a Dish entity.
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
        this.allergens = dish.getAllergensSet();

        if (dish.getRecipe() != null) {
            this.recipe = new RecipeDTO(dish.getRecipe());
        }
    }
}
