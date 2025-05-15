package dk.patientassist.utilities;

import java.util.List;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import jakarta.persistence.EntityManager;

/**
 * EmployeePopulator
 */
public class EmployeePopulator {

    public static void addAdmin() {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            List<Employee> shouldBeEmpty = em.createQuery("SELECT e FROM Employee e where e.email = ?1", Employee.class)
                    .setParameter(1, "admin@test.dk")
                    .getResultList();
            if (!shouldBeEmpty.isEmpty()) {
                return;
            }

            EmployeeDTO adminUser = new EmployeeDTO();
            adminUser.email = "admin@test.dk";
            adminUser.firstName = "admin";
            adminUser.middleName = "admin";
            adminUser.lastName = "admin";
            adminUser.setPassword("admin");
            adminUser.roles = new Role[] { Role.ADMIN };
            Employee adminEnt = Mapper.EmployeeDTOToEnt(adminUser);
            adminEnt.password = adminUser.hashPw();

            em.persist(adminEnt);
            em.getTransaction().commit();
        }
    }
}
