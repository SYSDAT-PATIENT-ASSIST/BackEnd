package dk.patientassist.persistence.dto;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class DishDTO
{
    private Integer id;
    private String name;
    private String description;
    private LocalDate available_from;
    private LocalDate available_until;
    private DishStatus status;

    public DishDTO(String name, String description, LocalDate available_from, LocalDate available_until, DishStatus status)
    {
        this.name = name;
        this.description = description;
        this.available_from = available_from;
        this.available_until = available_until;
        this.status = status;
    }

    public DishDTO(Dish dish)
    {
        this.name = dish.getName();
        this.description = dish.getDescription();
        this.available_from = dish.getAvailable_from();
        this.available_until = dish.getAvailable_until();
        this.status = dish.getStatus();
    }

}
