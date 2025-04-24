package dk.patientassist.control;

import dk.patientassist.persistence.dao.DishDAO;

public class DishController
{
    DishDAO dishDAO = new DishDAO();

    public void getAllAvailable()
    {
        dishDAO.getAllAvailable();
    }


}
