package dk.patientassist.security.daos;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.entities.User;
import dk.patientassist.security.exceptions.ValidationException;

public interface ISecurityDAO {
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    User createUser(String username, String password);
    User addRole(UserDTO user, String newRole);
}
