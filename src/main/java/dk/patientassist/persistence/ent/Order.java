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
    @Column(name = "bed_id")
    private Integer bed_id;
    @Column(name = "order_timer")
    private LocalDateTime order_time;
    @Column(name = "note")
    private String note;
    @Column(name = "orderStatus")
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
        //this.dish = new Dish(orderDTO.getDish());
        this.dish = new Dish(); //empty dish object
        this.dish.setId(orderDTO.getDish().getId()); //only set id
        this.status = orderDTO.getStatus();
    }

}
