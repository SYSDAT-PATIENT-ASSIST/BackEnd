package dk.patientassist.persistence.dao;

import dk.patientassist.control.OrderController;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest
{

    @BeforeEach
    void setUp()
    {
    }

    @AfterEach
    void tearDown()
    {
    }

    @Test
    void cancelOrder()
    {
        OrderDAO orderDAO = new OrderDAO();
        Dish dish = new Dish(3, "Kylling i karry", "godt med karry", LocalDate.ofYearDay(2025,24), LocalDate.now(), DishStatus.AVAILABLE);
        Order order = new Order(1, 201, LocalDateTime.now(), "Ingen allergier", dish, OrderStatus.PENDING); //id, bed_id, order_time, note, dish, status
        orderDAO.createOrder(order);

        Integer id = order.getId();

        orderDAO.cancelOrder(id);

        Order updatedOrder = orderDAO.getOrder(id);

        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    }
}