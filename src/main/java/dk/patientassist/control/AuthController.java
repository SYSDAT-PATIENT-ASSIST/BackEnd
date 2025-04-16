package dk.patientassist.control;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManager;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.time.Instant;
import java.util.Map;

/**
 * Patient Assist
 */
public class AuthController
{
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    public static EndpointGroup getEndpoints()
    {
        return () -> {
            path("/auth", () -> {
                post("/login", AuthController::login, Role.GUEST);
                post("/register", AuthController::register, Role.GUEST);
            });
        };
    }

    public static void init() throws JsonProcessingException
    {
        jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
        jwtIss = Utils.getConfigProperty("JWT_ISSUER");
        jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
        jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
    }

    private static void register(@NotNull Context ctx)
    {
        EntityManager em = null;
        try {
            JsonNode json = Utils.getObjectMapperCompact().readTree(ctx.body());
            Employee emp = Employee.fromJson(json);

            em = HibernateConfig.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            em.persist(emp);
            em.getTransaction().commit();
            em.close();

            String jwt = JWT.create()
                .withHeader(jwtHdr)
                .withIssuer(jwtIss)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(jwtExp))
                .withSubject("Patient Assist Employee Credentials")
                .withClaim("name", emp.getFullName())
                .withClaim("email", emp.email)
                .withClaim("roles", emp.getRolesAsString())
                .sign(Algorithm.HMAC256(jwtKey));

            JsonNode jsonResponse = Utils.getObjectMapperCompact().createObjectNode()
                .put("token", jwt)
                .put("email", emp.email)
                .put("name", emp.getFullName())
                .set("roles", emp.getRolesAsJSONArray());
            ctx.json(jsonResponse);
            ctx.status(201);
        } catch (Exception e) {
            if (em != null) {
                em.close();
            }
            logger.info("Registration failed: {}", e.getMessage());
            throw new UnauthorizedResponse("Registration failed");
        }
    }

    private static void login(@NotNull Context ctx)
    {
    }
}
