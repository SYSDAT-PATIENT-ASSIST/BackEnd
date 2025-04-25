package dk.patientassist.persistence.ent;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "_order")
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    private Integer bed_id;
    private LocalDateTime order_time;
    private String note;
    private OrderStatus status;


    @ManyToOne
    @JoinTable(name = "order_dish",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id"))
    private Dish dish;


    public Order(Integer bed_id, LocalDateTime order_time, String note, Dish dish, OrderStatus status){
        this.bed_id = bed_id;
        this.order_time = order_time;
        this.note = note;
        this.dish = dish;
        this.status = status;
    }

    public Order(OrderDTO orderDTO){
        this.bed_id = orderDTO.getBed_id();
        this.order_time = orderDTO.getOrder_time();
        this.note = orderDTO.getNote();
        this.dish = new Dish(orderDTO.getDish());
        this.status = orderDTO.getStatus();
    }

}
