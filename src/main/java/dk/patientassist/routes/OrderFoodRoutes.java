package dk.patientassist.routes;
import dk.patientassist.control.DishController;
import dk.patientassist.control.OrderController;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.patch;

public class OrderFoodRoutes
{
    private static final DishController dishcontroller = new DishController();
    private static final OrderController ordercontroller = new OrderController();

    public static EndpointGroup getOrderFoodRoutes() {
        return () -> {
            path("/orderfood", () -> {
                // --- GET ---
                get("/all-available", dishcontroller::getAllAvailable, Role.GUEST);

                // --- CREATE ---
                post("/create", ordercontroller::createOrder, Role.GUEST);

                // --- DELETE ---


                // --- PUT ---
                put("/cancel/{id}", ordercontroller::cancelOrder, Role.GUEST);
            });
        };
    }



}
