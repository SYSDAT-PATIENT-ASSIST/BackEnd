package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class OrderDAO
{

    List<Order> orders = new ArrayList<>();


    public void createOrder(Order order)
    {
        orders.add(order);
    }


    public void cancelOrder(Integer orderId)
    {
      Order order = getOrder(orderId);
      order.setStatus(OrderStatus.CANCELLED);
    }

    public Order getOrder(Integer orderId)
    {
      for (Order order : orders){
          if (order.getId().equals(orderId)){
              return order;
          }
      }
      return null;
    }



}
