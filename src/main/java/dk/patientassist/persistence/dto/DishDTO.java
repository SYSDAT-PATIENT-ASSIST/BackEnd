package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DishDTO {
    private Integer id;
    private String name;
    private String description;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private DishStatus status;
    private double kcal;
    private double protein;
    private double carbohydrates;
    private double fat;
    private Allergens allergens;

    public DishDTO(String name, String description, LocalDate availableFrom, LocalDate availableUntil,
                   DishStatus status, double kcal, double protein, double carbohydrates, double fat,
                   Allergens allergens) {
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

}
