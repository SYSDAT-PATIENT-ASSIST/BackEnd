package dk.patientassist.control;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.patientassist.config.HibernateConfig;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.ent.Section;
import dk.patientassist.security.enums.Role;
import dk.patientassist.service.Mapper;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.utilities.Utils;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

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
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            EmployeeDTO empDetails = ctx.bodyAsClass(EmployeeDTO.class);
            Employee emp = Mapper.EmployeeDTOToEnt(empDetails);
            emp.password = empDetails.hashPw();

            logger.info(new String(ctx.bodyAsBytes()));

            em.getTransaction().begin();

            List<Employee> empsWithSameEmail = em.createQuery("FROM Employee WHERE email ilike ?1", Employee.class)
                    .setParameter(1, emp.email).getResultList();
            if (empsWithSameEmail.size() > 0) {
                throw new EntityExistsException("");
            }

            if (empDetails.sections != null) {
                Section sect = null;
                for (var secId : empDetails.sections) {
                    if ((sect = em.find(Section.class, secId)) != null) {
                        emp.sections.add(sect);
                    } else {
                        logger.info("unable to find section {} in user's ({}) list of sections", secId,
                                empDetails.email);
                    }
                }
            }

            em.persist(emp);
            em.getTransaction().commit();

            String jwt = JWT.create()
                    .withHeader(jwtHdr)
                    .withIssuer(jwtIss)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusMillis(jwtExp))
                    .withSubject("Patient Assist Login Credentials")
                    .withClaim("name", emp.getFullName())
                    .withClaim("email", emp.email)
                    .withClaim("roles", emp.roles != null ? emp.getRolesAsStringList() : new ArrayList<>())
                    .withClaim("sectionIds",
                            emp.sections != null ? emp.sections.stream().map(s -> s.id).toList() : new ArrayList<>())
                    .sign(Algorithm.HMAC256(jwtKey));

            ObjectNode jsonResponse = Utils.getObjectMapperCompact().createObjectNode()
                    .put("token", jwt)
                    .put("email", emp.email)
                    .put("name", emp.getFullName());

            ctx.json(jsonResponse);
            ctx.status(201);
        } catch (EntityExistsException e) {
            logger.info(e.getMessage());
            throw new UnauthorizedResponse("Registration failed: user already exists with that email");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            throw new UnauthorizedResponse("Registration failed");
        }
    }

    private static void login(@NotNull Context ctx) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            EmployeeDTO empDetails = ctx.bodyAsClass(EmployeeDTO.class);

            logger.info(new String(ctx.bodyAsBytes()));

            em.getTransaction().begin();
            Employee emp = em.createQuery("FROM Employee WHERE email ILIKE ?1", Employee.class)
                    .setParameter(1, empDetails.email)
                    .getSingleResult();

            if (!empDetails.checkAgainstBCryptPw(emp.password)) {
                throw new UnauthorizedResponse("Invalid password");
            }

            String jwt = JWT.create()
                    .withHeader(jwtHdr)
                    .withIssuer(jwtIss)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusMillis(jwtExp))
                    .withSubject("Patient Assist Login Credentials")
                    .withClaim("name", emp.getFullName())
                    .withClaim("email", emp.email)
                    .withClaim("roles", emp.roles != null ? emp.getRolesAsStringList() : new ArrayList<>())
                    .withClaim("sectionIds",
                            emp.sections != null ? emp.sections.stream().map(s -> s.id).toList() : new ArrayList<>())
                    .sign(Algorithm.HMAC256(jwtKey));

            ObjectNode jsonResponse = Utils.getObjectMapperCompact().createObjectNode()
                    .put("token", jwt)
                    .put("email", emp.email)
                    .put("name", emp.getFullName());

            ctx.json(jsonResponse);
            ctx.status(200);
        } catch (EntityNotFoundException | NoResultException e) {
            logger.info("Login failed: {}", e.getMessage());
            throw new UnauthorizedResponse("Login failed: no user with that email");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Login failed: {}", e.getMessage());
            throw new UnauthorizedResponse("Login failed");
        }
    }
}
