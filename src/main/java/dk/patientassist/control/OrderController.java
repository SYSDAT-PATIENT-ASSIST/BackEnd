package dk.patientassist.control;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dao.OrderDAO;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.ent.Order;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.EntityManagerFactory;

public class OrderController{

    private final OrderDAO dao;

    public OrderController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = OrderDAO.getInstance(emf);
    }


    public void createOrder(Context ctx) {
        OrderDTO jsonRequest = ctx.bodyAsClass(OrderDTO.class);
        try {
            OrderDTO orderDTO = dao.createOrder(jsonRequest);
            ctx.res().setStatus(201);
            ctx.json(orderDTO, OrderDTO.class);
        } catch (Exception e) {
            throw new BadRequestResponse("Invalid request: " + e.getMessage());
        }
    }

    //used for cucumber / menuStepDefinitions
    public OrderDTO createOrder(OrderDTO dto) {
        return dao.createOrder(dto);
    }

    public void getOrder(Context ctx){
        int orderId = ctx.pathParamAsClass("id", Integer.class).get();
        try {
            OrderDTO orderDTO = dao.getOrder(orderId);
            ctx.res().setStatus(200);
            ctx.json(orderDTO, OrderDTO.class);
        } catch (Exception e) {
            throw new NotFoundResponse("No content found for this request");
        }
    }

    //used for cucumber / menuStepDefinitions
    public OrderDTO getOrder(Integer orderId){
        return dao.getOrder(orderId);
    }

    public void cancelOrder(Context ctx){
        int orderId = ctx.pathParamAsClass("id", Integer.class).get();
        try {
            OrderDTO orderDTO = dao.cancelOrder(orderId);
            ctx.res().setStatus(200);
            ctx.json(orderDTO, OrderDTO.class);
        } catch (Exception e) {
            throw new NotFoundResponse("No content found for this request");
        }
    }

    //used for cucumber / menuStepDefinitions
    public OrderDTO cancelOrder(Integer orderId){
        return dao.cancelOrder(orderId);
    }


}
