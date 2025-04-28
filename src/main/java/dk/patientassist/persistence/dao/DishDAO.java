package dk.patientassist.persistence.dao;
import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.OrderDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Order;
import dk.patientassist.persistence.enums.DishStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class DishDAO{

    private static DishDAO instance;
    private static EntityManagerFactory emf;

    public DishDAO(EntityManagerFactory _emf){
        emf = _emf;
    }

    public static DishDAO getInstance(EntityManagerFactory _emf){
        if (instance == null){
            emf = _emf;
            instance = new DishDAO(emf);
        }
        return instance;
    }


    //createDish only for test
    public DishDTO createDish(DishDTO dishDTO){
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Dish dish = new Dish(dishDTO);
            em.persist(dish);
            em.flush(); //making sure the ID is generated
            em.getTransaction().commit();
            return new DishDTO(dish);
        }
    }

    //getDish only for test
    public DishDTO getDish(Integer dishId){
        try (EntityManager em = emf.createEntityManager()){
            Dish dish = em.find(Dish.class, dishId);
            return new DishDTO(dish);
        }
    }

    public List<DishDTO> getAllAvailable(){
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<DishDTO> query = em.createQuery("SELECT new dk.patientassist.persistence.dto.DishDTO(d) FROM dk.patientassist.persistence.ent.Dish d WHERE d.status = dk.patientassist.persistence.enums.DishStatus.AVAILABLE OR d.status = dk.patientassist.persistence.enums.DishStatus.SOLD_OUT", DishDTO.class);
            return query.getResultList();
        }

    }



}
