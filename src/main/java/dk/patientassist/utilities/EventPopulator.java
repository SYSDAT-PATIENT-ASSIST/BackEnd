package dk.patientassist.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javafaker.Faker;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Event;
import jakarta.persistence.EntityManager;

/**
 * EventPopulator
 */
public class EventPopulator {
    private static final Logger logger = LoggerFactory.getLogger(EventPopulator.class);
    static Faker fakeGenerator = new Faker();
    static Random rng = new Random(System.currentTimeMillis());

    public static void populate(int max) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();

            List<Event> eventsFromDB = em.createQuery("FROM Event", Event.class).getResultList();

            if (eventsFromDB.size() >= max) {
                logger.info("adding 0 new events as there are already {} and {} were requested", eventsFromDB.size(),
                        max);
                return;
            }

            List<Event> events = new ArrayList<>();
            for (int i = 0; i < max - eventsFromDB.size(); i++) {
                events.add(MockData.event());
            }

            for (Event e : events) {
                em.persist(e);
            }

            logger.info("added {} new events to increase existing {} to {}", events.size(), eventsFromDB.size(), max);

            em.getTransaction().commit();
        } catch (Exception e) {
            logger.warn("error while attempting to populate events {}", e.getMessage());
        }
    }
}
