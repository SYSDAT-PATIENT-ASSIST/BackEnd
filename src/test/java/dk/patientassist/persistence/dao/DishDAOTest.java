package dk.patientassist.persistence.dao;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.config.Mode;
import dk.patientassist.persistence.dto.DishDTO;
import jakarta.persistence.EntityManagerFactory;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DishDAOTest {

    private static EntityManagerFactory emf;
    private static DishDAO dishDAO;

    @BeforeAll
    static void setUpAll() {
        HibernateConfig.init(Mode.TEST);
        emf = HibernateConfig.getEntityManagerFactory();
        dishDAO = DishDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAllAvailable() {
        List<DishDTO> dishes = dishDAO.getAll();
        Assert.assertEquals(0, dishes.size());
    }
}
