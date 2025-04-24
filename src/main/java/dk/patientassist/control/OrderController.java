package dk.patientassist.control;

import dk.patientassist.persistence.dao.OrderDAO;
import dk.patientassist.persistence.ent.Order;

public class OrderController
{

    OrderDAO orderDAO = new OrderDAO();


    public void createOrder(Order order)
    {
        orderDAO.createOrder(order);
    }

    public Order getOrder(Integer orderId)
    {
        Order order = orderDAO.getOrder(orderId);
        return order;
    }

    public void cancelOrder(Integer orderId)
    {
        orderDAO.cancelOrder(orderId);
    }


}
