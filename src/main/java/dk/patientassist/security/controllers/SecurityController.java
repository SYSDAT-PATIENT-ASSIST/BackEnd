package dk.patientassist.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import dk.patientassist.utilities.Utils;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.security.daos.ISecurityDAO;
import dk.patientassist.security.daos.SecurityDAO;
import dk.patientassist.security.entities.User;
import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.exceptions.NotAuthorizedException;
import dk.patientassist.security.exceptions.ValidationException;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.UserDTO;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

package dk.patientassist.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.daos.SecurityDAO;
import dk.patientassist.utilities.Utils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityController implements ISecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    private final ISecurityDAO securityDAO;
    private final ITokenSecurity tokenSecurity;
    private final ObjectMapper objectMapper;

    public SecurityController() {
        this(
            new SecurityDAO(HibernateConfig.getEntityManagerFactory()),
            new TokenSecurity(),
            new Utils().getObjectMapper()
        );
    }

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
                UserDTO user = ctx.bodyAsClass(UserDTO.class);

                if (user.getUsername() == null || user.getUsername().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty()) {
                    ctx.status(400);
                    ctx.json(returnObject.put("error", "Username and password must be provided"));
                    return;
                }

                UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                String token = createToken(verifiedUser);

                ctx.status(200)
                   .json(returnObject
                       .put("token", token)
                       .put("username", verifiedUser.getUsername())
                   );
            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                System.out.println(e.getMessage());
            }
        };
    }
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
                // Incorporate the EntityExistsException handling logic from US14
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
        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            ctx.status(200);
            return;
        }

        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header missing");
        }

        String[] headerParts = header.split(" ");
        if (headerParts.length != 2) {
            throw new UnauthorizedResponse("Authorization header malformed");
        }

        String token = headerParts[1];
        UserDTO verifiedTokenUser = verifyToken(token); // Assuming verifyToken handles token validation internally

        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid User or Token");
        }
        // If you need to log the user, you can add it here or within verifyToken
        // logger.info("User verified: " + verifiedTokenUser);
        ctx.attribute("user", verifiedTokenUser);
    };
}

@Override
// Check if the user's roles contain any of the allowed roles
public boolean authorize(UserDTO user, Set<RouteRole> allowedRoles) {
    if (user == null) {
        throw new UnauthorizedResponse("You need to log in, dude!");
    }
    Set<String> roleNames = allowedRoles.stream()
            .map(RouteRole::toString)  // Convert RouteRoles to Set of Strings
            .collect(Collectors.toSet());
    return user.getRoles().stream()
            .map(String::toUpperCase)
            .anyMatch(roleNames::contains);
}
@Override
public String createToken(UserDTO user) {
    try {
        String ISSUER;
        String TOKEN_EXPIRE_TIME;
        String SECRET_KEY;

        if (System.getenv("DEPLOYED") != null) {
            // Use environment variables when deployed
            ISSUER = System.getenv("ISSUER");
            TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
            SECRET_KEY = System.getenv("SECRET_KEY");
        } else {
            // Use config.properties for local development
            ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
            TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
            SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
        }
        return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
    } catch (Exception e) {
        throw new ApiException(500, "Could not create token");
    }
}

 @Override
public UserDTO verifyToken(String token) {
    boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
    String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

    try {
        if (!tokenSecurity.tokenIsValid(token, SECRET) || !tokenSecurity.tokenNotExpired(token)) {
            throw new UnauthorizedResponse("Token is not valid or expired");
        }
        return tokenSecurity.getUserWithRolesFromToken(token);
    } catch (ParseException | JOSEException | UnauthorizedResponse e) {
        throw new UnauthorizedResponse("Unauthorized. Could not verify token");
    }
}

@Override
public @NotNull Handler addRole() {
    return (ctx) -> {
        ObjectNode returnObject = objectMapper.createObjectNode();
        try {
            String newRole = ctx.bodyAsClass(ObjectNode.class).get("role").asText().toUpperCase();
            UserDTO requester = ctx.attribute("user");

            if ("ADMIN".equals(newRole) && (requester == null || !requester.getRoles().contains("ADMIN"))) {
                ctx.status(403).json(returnObject.put("msg", "Only ADMIN can assign that role"));
                return;
            }

            securityDAO.addRole(requester, newRole);
            ctx.status(200).json(returnObject.put("msg", "Role added: " + newRole));
        } catch (EntityNotFoundException e) {
            ctx.status(404).json(returnObject.put("msg", "User not found or role could not be added"));
        } catch (Exception e) {
            ctx.status(500).json(returnObject.put("msg", "Failed to add role: " + e.getMessage()));
        }
    };
}

public void healthCheck(@NotNull Context ctx) {
    ctx.status(200).json("{\"msg\": \"API is up and running\"}");
}
