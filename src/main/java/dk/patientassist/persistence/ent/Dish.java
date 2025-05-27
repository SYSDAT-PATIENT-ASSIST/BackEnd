package dk.patientassist.persistence.ent;

import dk.patientassist.service.dto.DishDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * JPA entity representing a dish in the system.
 * Stores nutritional values, availability, status, associated allergens, and
 * recipe.
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
     * Multiple allergens associated with this dish.
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = Allergens.class)
    @CollectionTable(name = "dish_allergens", joinColumns = @JoinColumn(name = "dish_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    private Set<Allergens> allergens;


    /**
     * One recipe associated with this dish.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;


    /**
     * Orders that include this dish.
     */
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    /**
     * Constructor used for quick dish instantiation (name, description,
     * availability, and status).
     *
     * @param name           the name of the dish
     * @param description    the description of the dish
     * @param availableFrom  the date the dish is available from
     * @param availableUntil the date the dish is available until
     * @param status         the dish status
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
     * Includes all nutritional and status fields. Recipe and orders must be set
     * separately.
     *
     * @param dto the DTO containing dish data
     * @throws IllegalArgumentException if required fields are null or invalid
     */
    public Dish(DishDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DishDTO cannot be null");
        if (dto.getName() == null || dto.getName().isBlank())
            throw new IllegalArgumentException("Dish name cannot be null or blank");
        if (dto.getDescription() == null)
            throw new IllegalArgumentException("Dish description cannot be null");
        if (dto.getAvailableFrom() == null || dto.getAvailableUntil() == null)
            throw new IllegalArgumentException("Available from/until dates cannot be null");
        if (dto.getAvailableFrom().isAfter(dto.getAvailableUntil()))
            throw new IllegalArgumentException("Available from date cannot be after available until date");

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
        // Recipe is set manually after construction (e.g., in DAO or service logic)
    }

    /**
     * Returns the set of allergens (used for DTO mapping).
     *
     * @return set of allergens
     */
    public Set<Allergens> getAllergensSet() {
        return allergens;
    }
}
