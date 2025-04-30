package dk.patientassist;

import dk.patientassist.config.ApplicationConfig;
import dk.patientassist.persistence.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

/**
 *
 * Patient Assist
 *
 */
public class App{
    private static EntityManagerFactory EMF;

    public static void main(String[] args){
        HibernateConfig.Init(HibernateConfig.Mode.DEV);
        EMF = HibernateConfig.getEntityManagerFactory();

        ApplicationConfig.startServer(7070);
    }
}