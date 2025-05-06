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

/**
 * JPA entity representing a dish in the system.
 * Stores nutritional values, status, availability, and allergen information.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "dish")
public class Dish {

    /**
     * Auto-generated ID of the dish.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    /**
     * Name of the dish.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Description of the dish.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * The date from which the dish is available.
     */
    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    /**
     * The date until which the dish is available.
     */
    @Column(name = "available_until", nullable = false)
    private LocalDate availableUntil;

    /**
     * Current status of the dish.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DishStatus status;

    /**
     * Energy content in kilocalories.
     */
    @Column(name = "kcal", nullable = false)
    private double kcal;

    /**
     * Protein content in grams.
     */
    @Column(name = "protein", nullable = false)
    private double protein;

    /**
     * Carbohydrate content in grams.
     */
    @Column(name = "carbohydrates", nullable = false)
    private double carbohydrates;

    /**
     * Fat content in grams.
     */
    @Column(name = "fat", nullable = false)
    private double fat;

    /**
     * Allergen type associated with this dish.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "allergens", nullable = false)
    private Allergens allergens;

    /**
     * Orders that include this dish.
     */
    @OneToMany(mappedBy = "dish")
    private List<Order> orders;

    /**
     * Recipe associated with this dish.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    /**
     * Constructor used for quick dish instantiation (status only).
     */
    public Dish(String name, String description, LocalDate availableFrom, LocalDate availableUntil, DishStatus status) {
        this.name = name;
        this.description = description;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.status = status;
    }

    /**
     * Constructs a Dish entity from a {@link DishDTO}.
     * Includes all nutritional and status fields.
     * @param dto the DTO containing dish data
     * @throws IllegalArgumentException if required fields are null or invalid
     */
    public Dish(DishDTO dto) {
        if (dto == null) throw new IllegalArgumentException("DishDTO cannot be null");
        if (dto.getName() == null || dto.getName().isBlank()) throw new IllegalArgumentException("Dish name cannot be null or blank");
        if (dto.getDescription() == null) throw new IllegalArgumentException("Dish description cannot be null");
        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null)
            throw new IllegalArgumentException("Available from/until dates cannot be null");
        if (dto.getAvailableFrom().isAfter(dto.getAvailableUntil()))
            throw new IllegalArgumentException("Available from date cannot be after available until date");
        if (dto.getStatus() == null) throw new IllegalArgumentException("Dish status cannot be null");
        if (dto.getAllergens() == null) throw new IllegalArgumentException("Allergens must be specified");

        this.name = dto.getName();
        this.description = dto.getDescription();
        this.availableFrom = dto.getAvailableFrom();
        this.availableUntil = dto.getAvailableUntil();
        this.status = dto.getStatus();
        this.kcal = dto.getKcal();
        this.protein = dto.getProtein();
        this.carbohydrates = dto.getCarbohydrates();
        this.fat = dto.getFat();
        this.allergens = dto.getAllergens();
    }
}
