package dk.patientassist.security.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.patientassist.control.DishController;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.utilities.Utils;
import dk.patientassist.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;

public class SecurityRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    public static EndpointGroup getSecurityRoutes() {
        return ()->{
            path("/auth", ()->{
                get("/healthcheck", securityController::healthCheck, Role.ANYONE);
                get("/test", ctx->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from Open Deployment")), Role.ANYONE);
                post("/login", securityController.login(), Role.ANYONE);
                post("/register", securityController.register(), Role.ANYONE);
                post("/user/addrole", securityController.addRole(), Role.ANYONE);
            });
        };
    }
    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                get("/user_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")), Role.ADMIN);
                get("/admin_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), Role.ADMIN);
            });
        };
    }

    public static EndpointGroup getDishRoutes() {
        return () -> {
            path("/dishes", () -> {
                get(ctx -> new DishController().getAllAvailableDishes(ctx), Role.ANYONE);
                get("/filter", ctx -> new DishController().getFilteredDishes(ctx), Role.ANYONE);
                get("{id}", ctx -> new DishController().getDishById(ctx), Role.ANYONE);

                post(ctx -> new DishController().createNewDish(ctx), Role.HEAD_CHEF, Role.ANYONE);
                put("{id}", ctx -> new DishController().updateExistingDish(ctx), Role.HEAD_CHEF, Role.ANYONE);
                delete("{id}", ctx -> new DishController().deleteExistingDish(ctx), Role.HEAD_CHEF, Role.ANYONE);

                patch("{id}/status", ctx -> new DishController().updateDishStatus(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/allergens", ctx -> new DishController().updateDishAllergens(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/name", ctx -> new DishController().updateDishName(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/description", ctx -> new DishController().updateDishDescription(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/kcal", ctx -> new DishController().updateDishKcal(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/protein", ctx -> new DishController().updateDishProtein(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/carbohydrates", ctx -> new DishController().updateDishCarbohydrates(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/fat", ctx -> new DishController().updateDishFat(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/available_from", ctx -> new DishController().updateDishAvailableFrom(ctx), Role.HEAD_CHEF, Role.ANYONE);
                patch("{id}/available_until", ctx -> new DishController().updateDishAvailableUntil(ctx), Role.HEAD_CHEF, Role.ANYONE);
            });
        };
    }

}