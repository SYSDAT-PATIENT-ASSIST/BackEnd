package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDAO{

    private static OrderDAO instance;
    private static EntityManagerFactory emf;

    public OrderDAO(EntityManagerFactory _emf){
        emf = _emf;
    }

    public static OrderDAO getInstance(EntityManagerFactory _emf){
        if (instance == null){
            emf = _emf;
            instance = new OrderDAO(emf);
        }
        return instance;
    }


    //createOrder only for test
    public OrderDTO createOrder(OrderDTO orderDTO){
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Order order = new Order(orderDTO);

            if(orderDTO.getDish() != null && orderDTO.getDish().getId() != null){
                Dish dish = em.find(Dish.class, orderDTO.getDish().getId());
                order.setDish(dish);
            }
            em.persist(order);
            em.getTransaction().commit();
            return new OrderDTO(order);
        }
    }


    public OrderDTO getOrder(Integer orderId){
        try (EntityManager em = emf.createEntityManager()){
            Order order = em.find(Order.class, orderId);
            return new OrderDTO(order);
        }
    }

    public OrderDTO cancelOrder(Integer orderId){
      try (EntityManager em = emf.createEntityManager()){
          em.getTransaction().begin();
          Order order = em.find(Order.class, orderId);
          order.setStatus(OrderStatus.ANNULLERET);
          em.merge(order);
          em.getTransaction().commit();
          return new OrderDTO(order);
      }
    }

    public OrderDTO updateOrderStatus(Integer integer, OrderStatus newStatus){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Order order = em.find(Order.class, integer);
            order.setStatus(newStatus);
            em.getTransaction().commit();
            return new OrderDTO(order);
        }
    }

    public List<OrderDTO> getAllWithDishes() {
        try (EntityManager em = emf.createEntityManager()) {
            List<Order> orders = em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.dish",
                    Order.class
            ).getResultList();

            em.clear();

            orders.forEach(order -> {
                Dish dish = order.getDish();
                dish.setRecipe(null);
            });

            return orders
                    .stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());
        }
    }


}
