package dk.patientassist.persistence.dao;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.dto.DishDTO;
import jakarta.persistence.EntityManagerFactory;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DishDAOTest{

    private static EntityManagerFactory emf;
    private static DishDAO dishDAO;

    @BeforeAll
    static void setUpAll(){
        HibernateConfig.Init(HibernateConfig.Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();
        dishDAO = new DishDAO(emf);
    }

    @BeforeEach
    void setUp(){
    }

    @AfterEach
    void tearDown(){
    }

    @Test
    void getAllAvailable(){
        List<DishDTO> dishes = dishDAO.getAllAvailableDishes();
        Assert.assertEquals(0, dishes.size());
    }
}