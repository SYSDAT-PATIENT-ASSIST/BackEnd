package dk.patientassist.control;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dao.DishDAO;
import dk.patientassist.persistence.dto.DishDTO;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class DishController
{
    private final DishDAO dao;

    public DishController(){
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = DishDAO.getInstance(emf);
    }

    public void getAllAvailable(Context ctx){
        try {
            List<DishDTO> dishDTOS = dao.getAllAvailable();
            ctx.res().setStatus(200);
            ctx.json(dishDTOS, DishDTO.class);
        } catch (Exception e) {
            throw new NotFoundResponse("No content found for this request");
        }
    }

    //used for cucumber / menuStepDefinitions
    public List<DishDTO> getAllAvailable(){
        return dao.getAllAvailable();
    }



}
