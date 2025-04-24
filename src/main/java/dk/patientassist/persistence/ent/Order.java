package dk.patientassist.persistence.ent;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Order
{
    private Integer id;
    private Integer bed_id;
    private LocalDateTime order_time;
    private String note;
    private Dish dish;
    private OrderStatus status;

    public Order(Integer id, Integer bed_id, LocalDateTime order_time, String note, Dish dish, OrderStatus status)
    {
        this.id = id;
        this.bed_id = bed_id;
        this.order_time = order_time;
        this.note = note;
        this.dish = dish;
        this.status = status;
    }

    public Order(OrderDTO orderDTO)
    {
        this.id = orderDTO.getId();
        this.bed_id = orderDTO.getBed_id();
        this.order_time = orderDTO.getOrder_time();
        this.note = orderDTO.getNote();
        this.dish = new Dish(orderDTO.getDish());
        this.status = orderDTO.getStatus();
    }

}
