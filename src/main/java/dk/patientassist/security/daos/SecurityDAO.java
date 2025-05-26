package dk.patientassist.security.daos;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.entities.Role;
import dk.patientassist.security.entities.User;
import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.exceptions.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.stream.Collectors;

public class SecurityDAO implements ISecurityDAO {

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }
            // force roles to load
            user.getRoles().size();
            if (!user.verifyPassword(password)) {
                throw new ValidationException("Wrong password");
            }
            return new UserDTO(
                    user.getUsername(),
                    user.getRoles().stream()
                            .map(r -> r.getRoleName())
                            .collect(Collectors.toSet())
            );
        }
    }

    @Override
    public User createUser(String username, String password) {
        EntityManager em = getEntityManager();
        try {
            // 1) Check if user already exists
            User existing = em.find(User.class, username);
            if (existing != null) {
                throw new EntityExistsException("User with username: " + username + " already exists");
            }

            // 2) Begin transaction and assign ADMIN as the default role
            em.getTransaction().begin();

            // Use the ADMIN enum value
            String defaultRoleName = dk.patientassist.security.enums.Role.ADMIN.name();
            Role adminRole = em.find(Role.class, defaultRoleName);
            if (adminRole == null) {
                adminRole = new Role(defaultRoleName);
                em.persist(adminRole);
            }

            // Create and persist the new user
            User newUser = new User(username, password);
            newUser.addRole(adminRole);
            em.persist(newUser);

            em.getTransaction().commit();
            return newUser;

        } catch (EntityExistsException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new ApiException(400, e.getMessage());
        } finally {
            em.close();
        }
    }


    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        EntityManager em = getEntityManager();
        try {
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            }
            em.getTransaction().begin();
            Role role = em.find(Role.class, newRole);
            if (role == null) {
                role = new Role(newRole);
                em.persist(role);
            }
            user.addRole(role);
            em.getTransaction().commit();
            return user;
        } finally {
            em.close();
        }
    }
}
