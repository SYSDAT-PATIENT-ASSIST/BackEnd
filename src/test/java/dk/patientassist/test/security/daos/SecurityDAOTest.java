package dk.patientassist.test.security.daos;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.daos.SecurityDAO;
import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import dk.patientassist.security.exceptions.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityDAO.
 */
class SecurityDAOTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;
    private SecurityDAO dao;

    /**
     * Set up mocks for EntityManagerFactory, EntityManager, and Transaction.
     */
    @BeforeEach
    void setUp() {
        emf = mock(EntityManagerFactory.class);
        em  = mock(EntityManager.class);
        tx  = mock(EntityTransaction.class);

        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tx);

        dao = new SecurityDAO(emf);
    }

    /**
     * getVerifiedUser should return a DTO when credentials match.
     */
    @Test
    void getVerifiedUser_success() throws Exception {
        String raw = "pw";
        String bcrypt = org.mindrot.jbcrypt.BCrypt.hashpw(raw, org.mindrot.jbcrypt.BCrypt.gensalt());

        User spyUser = spy(new User());
        spyUser.setUsername("bob");
        spyUser.setPassword(bcrypt);
        spyUser.addRole(new Role("USER"));
        doReturn(true).when(spyUser).verifyPassword(raw);

        when(em.find(User.class, "bob")).thenReturn(spyUser);

        UserDTO dto = dao.getVerifiedUser("bob", raw);
        assertEquals("bob", dto.getUsername());
        assertTrue(dto.getRoles().contains("USER"));

        verify(em).close();
    }

    /**
     * getVerifiedUser should throw if user not found.
     */
    @Test
    void getVerifiedUser_notFound() {
        when(em.find(User.class, "x")).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> dao.getVerifiedUser("x", "pw"));
        verify(em).close();
    }

    /**
     * getVerifiedUser should throw if password invalid.
     */
    @Test
    void getVerifiedUser_wrongPassword() {
        String raw = "right";
        String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(raw, org.mindrot.jbcrypt.BCrypt.gensalt());
        User u = new User();
        u.setUsername("a");
        u.setPassword(hashed);
        when(em.find(User.class, "a")).thenReturn(u);

        assertThrows(ValidationException.class,
                () -> dao.getVerifiedUser("a", "wrong"));
        verify(em).close();
    }

    /**
     * createUser should throw if username already exists.
     */
    @Test
    void createUser_alreadyExists() {
        when(em.find(User.class, "a")).thenReturn(new User());
        assertThrows(EntityExistsException.class,
                () -> dao.createUser("a", "pw"));
        verify(em).close();
    }

    /**
     * createUser should persist a new user with ADMIN as the default role.
     */
    @Test
    void createUser_success() {
        // No existing user
        when(em.find(User.class, "new")).thenReturn(null);
        // No existing ADMIN role
        when(em.find(Role.class, "ADMIN")).thenReturn(null);

        User u = dao.createUser("new", "pw");
        assertEquals("new", u.getUsername());
        assertTrue(u.getRolesAsStrings().contains("ADMIN"));

        InOrder ord = inOrder(em, tx);
        ord.verify(em).getTransaction();
        ord.verify(tx).begin();
        ord.verify(em).persist(any(Role.class)); // new ADMIN role
        ord.verify(em).persist(any(User.class));
        ord.verify(tx).commit();
        verify(em).close();
    }

    /**
     * addRole should create and persist a new Role entity.
     */
    @Test
    void addRole_newRoleAndUser() {
        UserDTO dto = new UserDTO("bob", Set.of("USER"));
        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword("pw");

        when(em.find(User.class, "bob")).thenReturn(bob);
        when(em.find(Role.class, "ADMIN")).thenReturn(null);

        dao.addRole(dto, "ADMIN");

        InOrder ord = inOrder(em, tx);
        ord.verify(em).getTransaction();
        ord.verify(tx).begin();
        ord.verify(em).persist(any(Role.class));
        ord.verify(tx).commit();
        verify(em).close();

        assertTrue(bob.getRolesAsStrings().contains("ADMIN"));
    }

    /**
     * addRole should reuse an existing Role entity.
     */
    @Test
    void addRole_existingRole() {
        UserDTO dto = new UserDTO("bob", Set.of("USER"));
        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword("pw");

        Role existing = new Role("ADMIN");
        when(em.find(User.class, "bob")).thenReturn(bob);
        when(em.find(Role.class, "ADMIN")).thenReturn(existing);

        User result = dao.addRole(dto, "ADMIN");
        assertEquals(bob, result);
        assertTrue(result.getRoles().contains(existing));

        InOrder ord = inOrder(em, tx);
        ord.verify(em).getTransaction();
        ord.verify(tx).begin();
        ord.verify(em, never()).persist(any(Role.class));
        ord.verify(tx).commit();
        verify(em).close();
    }

    /**
     * addRole should throw if user not found.
     */
    @Test
    void addRole_userNotFound() {
        UserDTO dto = new UserDTO("bob", Set.of("USER"));
        when(em.find(User.class, "bob")).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> dao.addRole(dto, "ADMIN"));
        verify(em).close();
    }
}
