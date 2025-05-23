package dk.patientassist.routes;

import dk.patientassist.control.DishController;
import dk.patientassist.control.OrderController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class OrderRoutes {
    private static final OrderController CONTROLLER = new OrderController();

    public static EndpointGroup getOrderRoutes(){
        return () -> {
            path("/api/orders", () -> {

                get("/", CONTROLLER::getAllOrdersWithDishes, Role.ANYONE);
                get("/{id}", CONTROLLER::getOrder, Role.ANYONE);

                put("/{id}", CONTROLLER::updateOrder, Role.ANYONE);

            });
        };
    }
}
