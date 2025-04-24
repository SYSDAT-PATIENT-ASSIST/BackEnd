package dk.patientassist.persistence.dao;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.ent.Dish;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DishDAOTest
{

    @BeforeEach
    void setUp()
    {
    }

    @AfterEach
    void tearDown()
    {
    }

    @Test
    void getAllAvailable()
    {
        DishDAO dishDAO = new DishDAO();
        List<Dish> dishes = dishDAO.getAllAvailable();
        Assert.assertEquals(0, dishes.size());
    }
}