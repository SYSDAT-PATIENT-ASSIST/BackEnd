package dk.patientassist.persistence.ent;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "dish")
public class Dish{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "available_from")
    private LocalDate available_from;

    @Column(name = "available_until")
    private LocalDate available_until;

    @Enumerated(EnumType.STRING)
    private DishStatus status;

    @Column(name = "kcal")
    private double kcal;

    @Column(name = "protein")
    private double protein;

    @Column(name = "carbohydrates")
    private double carbohydrates;

    @Column(name = "fat")
    private double fat;

    @Enumerated(EnumType.STRING)
    private Allergens allergens;

    @OneToMany(mappedBy = "dish")
    private List<Order> orders;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public Dish(String name, String description, LocalDate available_from, LocalDate available_until, DishStatus status){
        this.name = name;
        this.description = description;
        this.available_from = available_from;
        this.available_until = available_until;
        this.status = status;
    }

    public Dish(DishDTO dishDTO) {
        if (dishDTO == null) {
            throw new IllegalArgumentException("DishDTO cannot be null");
        }
        if (dishDTO.getName() == null || dishDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Dish name cannot be null or empty");
        }
        if (dishDTO.getDescription() == null) {
            throw new IllegalArgumentException("Dish description cannot be null");
        }
        if (dishDTO.getAvailable_from() == null || dishDTO.getAvailable_until() == null) {
            throw new IllegalArgumentException("Available from/until dates cannot be null");
        }
        if (dishDTO.getAvailable_from().isAfter(dishDTO.getAvailable_until())) {
            throw new IllegalArgumentException("Available from date cannot be after available until date");
        }
        if (dishDTO.getStatus() == null) {
            throw new IllegalArgumentException("Dish status cannot be null");
        }
        if (dishDTO.getAllergens() == null) {
            throw new IllegalArgumentException("Allergens must be specified");
        }

        this.name = dishDTO.getName();
        this.description = dishDTO.getDescription();
        this.available_from = dishDTO.getAvailable_from();
        this.available_until = dishDTO.getAvailable_until();
        this.status = dishDTO.getStatus();
        this.kcal = dishDTO.getKcal();
        this.protein = dishDTO.getProtein();
        this.carbohydrates = dishDTO.getCarbohydrates();
        this.fat = dishDTO.getFat();
        this.allergens = dishDTO.getAllergens();
    }
}