package dk.patientassist.control;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.ExamTreat;
import dk.patientassist.persistence.ent.ExamTreatCategory;
import dk.patientassist.persistence.ent.ExamTreatType;
import dk.patientassist.security.enums.Role;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.service.dto.ExamTreatTypeDTO;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;

/**
 * ExamTreatController
 */
public class ExamTreatController {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ExamTreatController.class);

    public static EndpointGroup getEndpoints() {
        return () -> {
            path("/examinations-and-treatments", () -> {
                get("/", ExamTreatController::readAllCategories, Role.GUEST);
                get("/categories", ExamTreatController::readAllCategories, Role.GUEST);
                get("/categories/{name}", ExamTreatController::readSubCategories, Role.GUEST);
                get("/articles/{name}", ExamTreatController::readArticle, Role.GUEST);
            });
        };
    }

    private static void readAllCategories(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            List<ExamTreatCategory> categories = em
                    .createQuery("SELECT c FROM ExamTreatCategory c", ExamTreatCategory.class)
                    .getResultList();
            em.close();
            ctx.status(200);
            ctx.json(categories.stream().map(Mapper::ExamTreatCategoryEntToDTO).toArray(ExamTreatCategoryDTO[]::new));
        } catch (Exception e) {
            throw new BadRequestResponse("Bad Request");
        }
    }

    private static void readSubCategories(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            String nameDecoded = URLDecoder.decode(ctx.pathParam("name"), StandardCharsets.UTF_8);
            List<ExamTreatType> types = em
                    .createQuery("SELECT t FROM ExamTreatType t where t.examTreatCategory.name ilike ?1",
                            ExamTreatType.class)
                    .setParameter(1, nameDecoded)
                    .getResultList();
            ctx.status(200);
            ctx.json(types.stream().map(Mapper::ExamTreatTypeEntToDTO).toArray(ExamTreatTypeDTO[]::new));
        } catch (Exception e) {
            throw new BadRequestResponse("Bad Request");
        }
    }

    private static void readArticle(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            String nameDecoded = URLDecoder.decode(ctx.pathParam("name"), StandardCharsets.UTF_8);
            List<ExamTreat> et = em
                    .createQuery("SELECT e FROM ExamTreat e where e.name ilike ?1", ExamTreat.class)
                    .setParameter(1, nameDecoded)
                    .getResultList();

            ctx.status(200);
            ctx.json(Mapper.ExamTreatEntToDTOFull(et.get(0)));
        } catch (Exception e) {
            throw new BadRequestResponse("Bad Request");
        }
    }

}
