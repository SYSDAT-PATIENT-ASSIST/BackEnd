package dk.patientassist.persistence.ent;
import dk.patientassist.persistence.dto.DishDTO;
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
public class Dish
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    private String name;
    private String description;
    private LocalDate available_from;
    private LocalDate available_until;
    private DishStatus status;

    @OneToMany(mappedBy = "dish")
    private List<Order> orders;

    public Dish(String name, String description, LocalDate available_from, LocalDate available_until, DishStatus status)
    {
        this.name = name;
        this.description = description;
        this.available_from = available_from;
        this.available_until = available_until;
        this.status = status;
    }

    public Dish(DishDTO dishDTO)
    {
        this.name = dishDTO.getName();
        this.description = dishDTO.getDescription();
        this.available_from = dishDTO.getAvailable_from();
        this.available_until = dishDTO.getAvailable_until();
        this.status = dishDTO.getStatus();
    }

}
