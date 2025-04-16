package dk.patientassist.control;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patient Assist
 */
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    public static EndpointGroup getEndpoints() {
        return () -> {
            path("/auth", () -> {
                post("/login", AuthController::login, Role.GUEST);
                post("/register", AuthController::register, Role.GUEST);
                /* TEST PATHS */
                get("/admin_only", ctx -> {
                    ctx.json("");
                    ctx.status(200);
                }, Role.ADMIN);
            });
        };
    }

    public static void init() throws JsonProcessingException {
        jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
        jwtIss = Utils.getConfigProperty("JWT_ISSUER");
        jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
        jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
    }

    private static void register(@NotNull Context ctx) {
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
                    .withSubject("Patient Assist Login Credentials")
                    .withClaim("name", emp.getFullName())
                    .withClaim("email", emp.email)
                    .withClaim("roles", emp.getRolesAsStringList())
                    .sign(Algorithm.HMAC256(jwtKey));

            JsonNode jsonResponse = Utils.getObjectMapperCompact().createObjectNode()
                    .put("token", jwt)
                    .put("email", emp.email)
                    .put("name", emp.getFullName())
                    .set("roles", emp.getRolesAsJSONArray());
            ctx.json(jsonResponse);
            ctx.status(201);
        } catch (ConstraintViolationException e) {
            throw new UnauthorizedResponse("registration failed: user already exists with that email");
        } catch (Exception e) {
            throw new UnauthorizedResponse("registration failed");
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    private static void login(@NotNull Context ctx) {
        EntityManager em = null;
        try {
            JsonNode json = Utils.getObjectMapperCompact().readTree(ctx.body());
            String email = json.get("email").asText();
            String password = json.get("password").asText();

            em = HibernateConfig.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();
            Employee emp = em.createQuery("FROM Employee WHERE email ILIKE ?1", Employee.class)
                    .setParameter(1, email)
                    .getSingleResult();
            em.close();

            if (!BCrypt.checkpw(password, emp.password)) {
                throw new UnauthorizedResponse("Invalid email or password");
            }

            String jwt = JWT.create()
                    .withHeader(jwtHdr)
                    .withIssuer(jwtIss)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusMillis(jwtExp))
                    .withSubject("Patient Assist Login Credentials")
                    .withClaim("name", emp.getFullName())
                    .withClaim("email", emp.email)
                    .withClaim("roles", emp.getRolesAsStringList())
                    .sign(Algorithm.HMAC256(jwtKey));

            JsonNode jsonResponse = Utils.getObjectMapperCompact()
                    .createObjectNode()
                    .put("token", jwt)
                    .put("email", emp.email)
                    .put("name", emp.getFullName())
                    .set("roles", emp.getRolesAsJSONArray());

            ctx.json(jsonResponse);
            ctx.status(200);
        } catch (Exception e) {
            if (em != null) {
                em.close();
            }
            logger.info("Login failed: {}", e.getMessage());
            throw new UnauthorizedResponse("Login failed");
        }
    }
}
