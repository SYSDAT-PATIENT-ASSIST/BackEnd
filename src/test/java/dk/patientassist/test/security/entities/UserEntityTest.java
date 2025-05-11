package dk.patientassist.test.security.entities;

import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 */
class UserEntityTest {

    /**
     * verifyPassword() should return true on match, false otherwise.
     */
    @Test
    void verifyPassword_trueAndFalse() {
        User u = new User("u", "plaintext");
        assertTrue(u.verifyPassword("plaintext"));
        assertFalse(u.verifyPassword("wrong"));
    }

    /**
     * addRole() and removeRole() should update the role set.
     */
    @Test
    void addAndRemoveRole() {
        User u = new User("u", "pw");
        Role r1 = new Role("A"), r2 = new Role("B");

        u.addRole(r1);
        u.addRole(r2);
        assertEquals(Set.of("A","B"), u.getRolesAsStrings());

        u.removeRole("A");
        assertEquals(Set.of("B"), u.getRolesAsStrings());
    }

    /**
     * getRolesAsStrings() returns null when no roles assigned.
     */
    @Test
    void getRolesAsStrings_empty() {
        User u = new User("u", "pw");
        assertNull(u.getRolesAsStrings());
    }
}
