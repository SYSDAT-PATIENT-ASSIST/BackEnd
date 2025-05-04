package dk.patientassist.control;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.utilities.Utils;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Patient Assist
 */
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    public static EndpointGroup getEndpoints() {
        return () -> {
            path("/events", () -> {
                get("/", EventController::readAll, Role.GUEST);
                get("/{eventId}", EventController::read, Role.GUEST);
                put("/", EventController::create, Role.ADMIN);
                patch("/{eventId}", EventController::update, Role.ADMIN);
                delete("/{eventId}", EventController::remove, Role.ADMIN);
            });
        };
    }

    private static void create(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            EventDTO dto = ctx.bodyAsClass(EventDTO.class);
            Event event = Mapper.EventDTOToEnt(dto);
            if (event.id != null) {
                event.id = null;
            }

            em.getTransaction().begin();
            em.persist(event);
            em.getTransaction().commit();

            ctx.status(201);
            ctx.json(Mapper.EventEntToDTO(event));

            logger.info("created new event {}",
                    Utils.getObjectMapperCompact().writeValueAsString(Mapper.EventEntToDTO(event)));
        } catch (Exception e) {
            throw new BadRequestResponse("could not create event");
        }
    }

    private static void readAll(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            List<Event> events = em.createQuery("SELECT e FROM Event e", Event.class)
                    .getResultList();
            ctx.status(200);
            ctx.json(events.stream().map(Mapper::EventEntToDTO).toArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestResponse("could not retrieve events");
        }
    }

    private static void read(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            Integer id = Integer.parseInt(ctx.pathParam("eventId"));

            em.getTransaction().begin();
            Event event = em.createQuery("SELECT e FROM Event e WHERE e.id = ?1", Event.class)
                    .setParameter(1, id)
                    .getSingleResult();
            ctx.status(200);
            ctx.json(event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestResponse("could not find event");
        }
    }

    private static void update(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            Integer id = Integer.parseInt(ctx.pathParam("eventId"));
            EventDTO dto = ctx.bodyAsClass(EventDTO.class);

            em.getTransaction().begin();
            Event event = em.createQuery("SELECT e FROM Event e WHERE e.id = ?1", Event.class)
                    .setParameter(1, id)
                    .getSingleResult();
            event.name = dto.name;
            event.description = dto.description;
            event.startTime = dto.startTime;
            event.duration = dto.duration;
            em.merge(event);
            em.getTransaction().commit();

            ctx.status(200);
            ctx.json(Mapper.EventEntToDTO(event));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestResponse("could not update event");
        }
    }

    private static void remove(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            Integer id = Integer.parseInt(ctx.pathParam("eventId"));

            em.getTransaction().begin();
            Event event = em.createQuery("SELECT e FROM Event e WHERE e.id = ?1", Event.class)
                    .setParameter(1, id)
                    .getSingleResult();
            em.remove(event);
            em.getTransaction().commit();
            ctx.status(200);
            ctx.json(Mapper.EventEntToDTO(event));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestResponse("could not find event to delete");
        }
    }
}
