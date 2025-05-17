package dk.patientassist.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.daos.ISecurityDAO;
import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.exceptions.NotAuthorizedException;
import dk.patientassist.security.exceptions.ValidationException;
import dk.patientassist.utilities.Utils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles login, registration, token creation/verification, and role‐assignment.
 * Now uses constructor injection for easier unit testing.
 */
public class SecurityController implements ISecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    private final ISecurityDAO securityDAO;
    private final ITokenSecurity tokenSecurity;
    private final ObjectMapper objectMapper;

    /**
     * Production constructor: uses real DAO, real TokenSecurity, and the shared ObjectMapper.
     */
    public SecurityController() {
        this(
                // real Hibernate‐backed DAO:
                new dk.patientassist.security.daos.SecurityDAO(dk.patientassist.persistence.HibernateConfig.getEntityManagerFactory()),
                // real JWT/token implementation:
                new dk.bugelhartmann.TokenSecurity(),
                // ObjectMapper from your Utils class:
                new Utils().getObjectMapper()
        );
    }

    /**
     * Injectable constructor for tests.
     */
    public SecurityController(ISecurityDAO securityDAO, ITokenSecurity tokenSecurity, ObjectMapper objectMapper) {
        this.securityDAO   = securityDAO;
        this.tokenSecurity = tokenSecurity;
        this.objectMapper  = objectMapper;
    }


    @Override
    public Handler login() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                UserDTO request = ctx.bodyAsClass(UserDTO.class);
                UserDTO verified = securityDAO.getVerifiedUser(request.getUsername(), request.getPassword());
                String token = createToken(verified);

                // chain status + json
                ctx.status(200)
                        .json(returnObject
                                .put("token", token)
                                .put("username", verified.getUsername()));
            } catch (jakarta.persistence.EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }

    @Override
    public Handler register() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            var em = dk.patientassist.persistence.HibernateConfig.getEntityManagerFactory().createEntityManager();
            try {
                UserDTO input = ctx.bodyAsClass(UserDTO.class);
                String roleParam = ctx.queryParam("role").toUpperCase();

                // validate role
                dk.patientassist.security.enums.Role validated;
                try {
                    validated = dk.patientassist.security.enums.Role.valueOf(roleParam);
                } catch (IllegalArgumentException iae) {
                    ctx.status(HttpStatus.BAD_REQUEST)
                            .json(returnObject.put("msg", "Invalid role: " + roleParam));
                    return;
                }

                em.getTransaction().begin();

                if (em.find(dk.patientassist.security.entities.User.class, input.getUsername()) != null) {
                    ctx.status(HttpStatus.UNPROCESSABLE_CONTENT)
                            .json(returnObject.put("msg", "User already exists"));
                    return;
                }

                var newUser = new dk.patientassist.security.entities.User();
                newUser.setUsername(input.getUsername());
                newUser.setPassword(
                        org.mindrot.jbcrypt.BCrypt.hashpw(input.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt())
                );

                // load or create role entity
                var roleEntity = em.find(dk.patientassist.security.entities.Role.class, validated.name());
                if (roleEntity == null) {
                    roleEntity = new dk.patientassist.security.entities.Role(validated.name());
                    em.persist(roleEntity);
                }

                newUser.addRole(roleEntity);
                em.persist(newUser);
                em.getTransaction().commit();

                // generate token for new user
                String token = createToken(new UserDTO(newUser.getUsername(), Set.of(validated.name())));
                ctx.status(HttpStatus.CREATED)
                        .json(returnObject.put("token", token).put("username", newUser.getUsername()));
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(returnObject.put("msg", "Registration failed"));
            } finally {
                em.close();
            }
        };
    }

    @Override
    public Handler authenticate() {
        return ctx -> {
            if ("OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            if (header == null) throw new UnauthorizedResponse("Authorization header missing");
            String[] parts = header.split(" ");
            if (parts.length != 2) throw new UnauthorizedResponse("Authorization header malformed");
            UserDTO user = verifyToken(parts[1]);
            ctx.attribute("user", user);
        };
    }

    @Override
    public boolean authorize(UserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) throw new UnauthorizedResponse("You need to log in!");
        Set<String> allowed = allowedRoles.stream()
                .map(RouteRole::toString)
                .collect(Collectors.toSet());
        return user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(allowed::contains);
    }

    @Override
    public String createToken(UserDTO user) {
        try {
            String issuer = Utils.getPropertyValue("ISSUER", "config.properties");
            String expire = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
            String secret = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            return tokenSecurity.createToken(user, issuer, expire, secret);
        } catch (Exception e) {
            throw new ApiException(500, "Could not create token");
        }
    }

    @Override
    public UserDTO verifyToken(String token) {
        try {
            String secret = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            if (!tokenSecurity.tokenIsValid(token, secret) || !tokenSecurity.tokenNotExpired(token)) {
                throw new UnauthorizedResponse("Token is not valid or expired");
            }
            return tokenSecurity.getUserWithRolesFromToken(token);
        } catch (ParseException | JOSEException | UnauthorizedResponse e) {
            throw new UnauthorizedResponse("Unauthorized. Could not verify token");
        }
    }

    public Handler addRole() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            UserDTO requester = ctx.attribute("user");
            String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText().toUpperCase();

            // only ADMIN can assign ADMIN
            if ("ADMIN".equals(newRole) && (requester == null || !requester.getRoles().contains("ADMIN"))) {
                ctx.status(403).json(returnObject.put("msg", "Only ADMIN can assign that role"));
                return;
            }

            securityDAO.addRole(requester, newRole);
            ctx.status(200).json(returnObject.put("msg", "Role added: " + newRole));
        };
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
