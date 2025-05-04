package dk.patientassist.utilities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javafaker.Faker;
import com.github.javafaker.HarryPotter;

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
                Event e = new Event();
                HarryPotter hp = fakeGenerator.harryPotter();
                e.name = hp.location();
                e.description = hp.quote();
                e.startTime = LocalDateTime.now().plusDays(rng.nextLong(-365, 365)).plusHours(rng.nextLong(-24, 24));
                e.duration = Duration.ofMinutes(rng.nextLong(30, 300));
                events.add(e);
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
