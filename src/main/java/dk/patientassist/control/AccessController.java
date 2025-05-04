package dk.patientassist.control;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Patient Assist
 */
public class AccessController {

    private static final Logger logger = LoggerFactory.getLogger(AccessController.class);

    static String jwtKey;
    static String jwtIss;
    static Long jwtExp;
    static String jwtHdr;

    public static void init() throws JsonProcessingException {
        jwtKey = Utils.getConfigProperty("JWT_SECRET_KEY");
        jwtIss = Utils.getConfigProperty("JWT_ISSUER");
        jwtExp = Long.parseLong(Utils.getConfigProperty("JWT_EXPIRE_TIME"));
        jwtHdr = Utils.getObjectMapperCompact().writeValueAsString(Map.of("typ", "JWT", "alg", "HS256"));
    }

    public static void check(@NotNull Context ctx) {
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.GUEST)) {
            return;
        }

        try {
            String jwtStr = ctx.header("Authorization").split(" ")[1];
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(jwtKey))
                    .withIssuer(jwtIss)
                    .build();
            DecodedJWT jwt = jwtVerifier.verify(jwtStr);

            if (jwt.getExpiresAtAsInstant().compareTo(Instant.now()) < 0) {
                throw new ForbiddenResponse("Forbidden: token expired");
            }

            Claim claimRoles = jwt.getClaim("roles");
            if (claimRoles == null) {
                throw new ForbiddenResponse("Forbidden: roles do not match endpoint rules");
            }

            Role[] roles = claimRoles.asArray(Role.class);
            if (claimRoles != null) {
                ctx.attribute("roles", roles);
                logger.info("employee {} with roles {}", jwt.getClaim("email").asString(), Role.stringify(roles));
            }

            if (!Role.find(ctx.routeRoles(), roles)) {
                throw new ForbiddenResponse("Forbidden: roles do not match endpoint rules");
            }

            logger.info("access granted to {} from {} ({})", ctx.path(), ctx.ip(), jwt.toString());
        } catch (JWTVerificationException e) {
            logger.info("access denied to {} from {}: {}", ctx.path(), ctx.ip(), e.getMessage());
            throw new ForbiddenResponse("Forbidden: " + e.getMessage());
        } catch (ForbiddenResponse e) {
            logger.info("access denied to {} from {}", ctx.path(), ctx.ip());
            throw new ForbiddenResponse(e.getMessage());
        } catch (Exception e) {
            logger.info("access denied to {} from {}", ctx.path(), ctx.ip());
            throw new ForbiddenResponse("Forbidden");
        }
    }
}
