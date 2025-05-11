package dk.patientassist.test.persistence;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.HibernateConfig.Mode;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HibernateConfig.
 */
class HibernateConfigTest {

    /**
     * Reset the private static emf after each test so tests stay isolated.
     */
    @AfterEach
    void tearDown() throws Exception {
        Field emfField = HibernateConfig.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);
    }

    /**
     * Before calling Init(), getEntityManagerFactory() should throw.
     */
    @Test
    void getEntityManagerFactory_notInitialized_throws() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                HibernateConfig::getEntityManagerFactory);
        assertTrue(ex.getMessage().contains("No EntityManagerFactory Instance"));
    }

    /**
     * Init(TEST) should spin up Testcontainers Postgres and return a working EMF.
     */
    @Test
    void initTestMode_providesEntityManagerFactory() {
        HibernateConfig.Init(Mode.TEST);
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        assertNotNull(emf);

        var em = emf.createEntityManager();
        assertTrue(em.isOpen());
        em.close();
        assertFalse(em.isOpen());
    }
}
