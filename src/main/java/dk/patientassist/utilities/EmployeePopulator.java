package dk.patientassist.utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import com.github.javafaker.Faker;

import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.security.enums.Role;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import jakarta.persistence.EntityManager;

/**
 * EmployeePopulator
 */
public class EmployeePopulator {

    static Faker faker = new Faker();

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

    public static void populate() {
        addEmployee("læge", "1234", dk.patientassist.security.enums.Role.DOCTOR);
        addEmployee("sygeplejerske", "1234", dk.patientassist.security.enums.Role.NURSE);
        addEmployee("kok", "1234", dk.patientassist.security.enums.Role.CHEF);
        addEmployee("hovedkok", "1234", dk.patientassist.security.enums.Role.HEAD_CHEF);
        addEmployee("køkken", "1234", dk.patientassist.security.enums.Role.KITCHEN_STAFF);
    }

    static void addEmployee(String email, String pw, Role role) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            List<Employee> shouldBeEmpty = em.createQuery("SELECT e FROM Employee e where e.email = ?1", Employee.class)
                    .setParameter(1, email)
                    .getResultList();
            if (!shouldBeEmpty.isEmpty()) {
                return;
            }
            var name = faker.name();
            Employee emp = new Employee();
            emp.email = email;
            emp.firstName = name.firstName();
            emp.lastName = name.lastName();
            emp.password = BCrypt.hashpw(pw, BCrypt.gensalt());
            emp.roles = Set.of(role);
            em.persist(emp);
            em.getTransaction().commit();
        }
    }
}
