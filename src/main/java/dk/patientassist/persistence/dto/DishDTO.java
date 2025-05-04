package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DishDTO {

    private Integer id;
    private String name;
    private String description;
    private LocalDate available_from;
    private LocalDate available_until;
    private DishStatus status;
    private double kcal;
    private double protein;
    private double carbohydrates;
    private double fat;
    private Allergens allergens;

    public DishDTO(String name, String description, LocalDate available_from, LocalDate available_until,
                   DishStatus status, double kcal, double protein, double carbohydrates, double fat, Allergens allergens) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (available_from == null || available_until == null) {
            throw new IllegalArgumentException("Available from/until dates cannot be null");
        }
        if (available_from.isAfter(available_until)) {
            throw new IllegalArgumentException("Available from date cannot be after available until date");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (allergens == null) {
            throw new IllegalArgumentException("Allergens cannot be null");
        }

        this.name = name;
        this.description = description;
        this.available_from = available_from;
        this.available_until = available_until;
        this.status = status;
        this.kcal = kcal;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.allergens = allergens;
    }

    public DishDTO(Dish dish) {
        this.id = dish.getId();
        this.name = dish.getName();
        this.description = dish.getDescription();
        this.available_from = dish.getAvailable_from();
        this.available_until = dish.getAvailable_until();
        this.status = dish.getStatus();
        this.kcal = dish.getKcal();
        this.protein = dish.getProtein();
        this.carbohydrates = dish.getCarbohydrates();
        this.fat = dish.getFat();
        this.allergens = dish.getAllergens();
    }
}