package dk.patientassist.service.dto;

import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class OrderDTO {
    private Integer id;
    private Integer bed_id;
    private LocalDateTime order_time;
    private String note;
    private DishDTO dish;
    private OrderStatus status;

    public OrderDTO(Integer bed_id, LocalDateTime order_time, String note, DishDTO dish, OrderStatus status) {
        this.bed_id = bed_id;
        this.order_time = order_time;
        this.note = note;
        this.dish = dish;
        this.status = status;
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.bed_id = order.getBed_id();
        this.order_time = order.getOrder_time();
        this.note = order.getNote();
        this.dish = new DishDTO(order.getDish());
        this.status = order.getStatus();
    }

}
