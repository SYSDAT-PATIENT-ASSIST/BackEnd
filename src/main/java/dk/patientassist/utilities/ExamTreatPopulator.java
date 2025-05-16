package dk.patientassist.utilities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.ExamTreatCategory;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import jakarta.persistence.EntityManager;

/**
 * ExamTreatPopulator
 */
public class ExamTreatPopulator {

    public static void load(String filePath) throws IOException, URISyntaxException {

        URL urlOfFilePath = WebScraper.class.getClassLoader().getResource(filePath);
        String fileAsStr = new String(Files.readAllBytes(Paths.get(urlOfFilePath.toURI())));

        ExamTreatCategoryDTO[] ETCats = Utils.getObjectMapperCompact().readValue(fileAsStr,
                ExamTreatCategoryDTO[].class);

        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();

            List<ExamTreatCategory> categories = new ArrayList<>();
            for (var etc : ETCats) {
                categories.add(Mapper.ExamTreatCategoryDTOToEnt(etc));
            }

            for (var cat : categories) {
                em.persist(cat);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("ExamTreatPopulator.load(): Nothing to do...");
        }
    }
}
