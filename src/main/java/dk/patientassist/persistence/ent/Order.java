package dk.patientassist.persistence.ent;

import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a placed food order for a specific hospital bed.
 * Each order is linked to one {@link Dish} and tracks status and notes.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "_order") // underscores to avoid SQL keyword conflict
public class Order {

    /**
     * Unique identifier for the order.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    /**
     * Bed ID that the order is associated with.
     */
    @Column(name = "bed_id", nullable = false)
    private Integer bed_id;

    /**
     * Timestamp when the order was placed.
     */
    @Column(name = "order_time", nullable = false)
    private LocalDateTime order_time;

    /**
     * Optional note to the kitchen or staff.
     */
    @Column(name = "note")
    private String note;

    /**
     * Current status of the order (e.g., PENDING, DELIVERED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    /**
     * Dish associated with this order.
     * Mapped using a join table `order_dish` to maintain flexibility for future many-to-many extensions.
     */
    @ManyToOne(optional = false)
    @JoinTable(
            name = "order_dish",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    private Dish dish;

    /**
     * Full constructor for direct instantiation of an Order entity.
     *
     * @param bed_id     the bed receiving the dish
     * @param order_time the timestamp the order was placed
     * @param note       optional note
     * @param dish       the associated dish
     * @param status     the order's current status
     */
    public Order(Integer bed_id, LocalDateTime order_time, String note, Dish dish, OrderStatus status) {
        this.bed_id = bed_id;
        this.order_time = order_time;
        this.note = note;
        this.dish = dish;
        this.status = status;
    }

    /**
     * Constructs an Order entity from a {@link OrderDTO}.
     * Only sets the dish ID for binding (avoids loading full entity).
     *
     * @param orderDTO the DTO containing order data
     */
    public Order(OrderDTO orderDTO) {
        this.bed_id = orderDTO.getBed_id();
        this.order_time = orderDTO.getOrder_time();
        this.note = orderDTO.getNote();
        this.dish = new Dish(); // placeholder entity with only ID
        this.dish.setId(orderDTO.getDish().getId());
        this.status = orderDTO.getStatus();
    }
}
