package dk.patientassist.test.security.daos;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.HibernateConfig.Mode;
import dk.patientassist.security.daos.SecurityDAO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityDAOIT {

    private SecurityDAO dao;

    @BeforeAll
    void beforeAll() {
        // Start Testcontainers + Hibernate in TEST mode
        HibernateConfig.Init(Mode.TEST);
        dao = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    }

    @Test
    void createUser_thenGetVerifiedUser() throws Exception {
        // Create a brand-new user
        dao.createUser("it_bob", "pass123");

        // Fetch them back
        UserDTO fetched = dao.getVerifiedUser("it_bob", "pass123");
        assertEquals("it_bob", fetched.getUsername());

        // Default role is ADMIN
        assertTrue(fetched.getRoles().contains("ADMIN"),
                "Expected new users to have ADMIN by default");
    }

    @Test
    void createUser_alreadyExists_throws() {
        dao.createUser("alice", "pw");
        assertThrows(EntityExistsException.class,
                () -> dao.createUser("alice", "pw"));
    }

    @Test
    void addRole_andGetVerifiedUser_seesNewHOVEDKOKRole() throws Exception {
        // 1) Seed a fresh user (default ADMIN)
        dao.createUser("it_charlie", "secret");
        UserDTO before = dao.getVerifiedUser("it_charlie", "secret");
        assertTrue(before.getRoles().contains("ADMIN"),
                "Default role should be ADMIN");

        // 2) Now add HOVEDKOK on top of ADMIN
        dao.addRole(before, "HOVEDKOK");

        // 3) Fetch again—should have both ADMIN and HOVEDKOK
        UserDTO after = dao.getVerifiedUser("it_charlie", "secret");
        assertTrue(after.getRoles().contains("ADMIN"),   "Still has ADMIN");
        assertTrue(after.getRoles().contains("HOVEDKOK"),"Now also has HOVEDKOK");
    }

    @Test
    void getVerifiedUser_nonexistent_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> dao.getVerifiedUser("no_such_user", "irrelevant"));
    }
}
