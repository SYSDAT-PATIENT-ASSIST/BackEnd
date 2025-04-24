package dk.patientassist.persistence.ent;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.enums.DishStatus;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class Dish
{
    private Integer id;
    private String name;
    private String description;
    private LocalDate available_from;
    private LocalDate available_until;
    private DishStatus status;

    public Dish(Integer id, String name, String description, LocalDate available_from, LocalDate available_until, DishStatus status)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available_from = available_from;
        this.available_until = available_until;
        this.status = status;
    }

    public Dish(DishDTO dishDTO)
    {
        this.id = dishDTO.getId();
        this.name = dishDTO.getName();
        this.description = dishDTO.getDescription();
        this.available_from = dishDTO.getAvailable_from();
        this.available_until = dishDTO.getAvailable_until();
        this.status = dishDTO.getStatus();
    }

}
