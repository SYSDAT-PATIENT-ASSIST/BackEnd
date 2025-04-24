package dk.patientassist.persistence.dao;

import dk.patientassist.persistence.ent.Dish;

import java.util.ArrayList;
import java.util.List;

public class DishDAO
{
    List<Dish> listofdishes = new ArrayList<>();

    public List<Dish> getAllAvailable()
    {
        return listofdishes;

    }


}
