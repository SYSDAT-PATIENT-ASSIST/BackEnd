package dk.patientassist.persistence.dao;

import dk.patientassist.control.OrderController;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.DishStatus;
import dk.patientassist.persistence.enums.OrderStatus;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest{

    private static EntityManagerFactory emf;
    private static OrderDAO orderDAO;
    private static DishDAO dishDAO;

    @BeforeAll
    static void setUpAll(){
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();
        orderDAO = new OrderDAO(emf);
        dishDAO = new DishDAO(emf);
    }

    @BeforeEach
    void setUp(){
    }

    @AfterEach
    void tearDown(){
    }

    @Test
    void cancelOrder(){
        DishDTO dish = new DishDTO("Kylling i karry", "godt med karry", LocalDate.ofYearDay(2025,24), LocalDate.now(), DishStatus.AVAILABLE);
        DishDTO savedDish = dishDAO.createDish(dish);
        OrderDTO order = new OrderDTO(201, LocalDateTime.now(), "Ingen allergier", savedDish, OrderStatus.PENDING); //id, bed_id, order_time, note, dish, status
        orderDAO.createOrder(order);

        Integer id = order.getId();

        orderDAO.cancelOrder(id);

        OrderDTO updatedOrder = orderDAO.getOrder(id);

        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    }
}