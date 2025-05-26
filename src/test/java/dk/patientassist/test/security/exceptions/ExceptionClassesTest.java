package dk.patientassist.test.security.exceptions;

import dk.patientassist.security.exceptions.ApiException;
import dk.patientassist.security.exceptions.NotAuthorizedException;
import dk.patientassist.security.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exception classes.
 */
class ExceptionClassesTest {

    /**
     * ApiException should carry code and message.
     */
    @Test
    void apiException_holdsCodeAndMessage() {
        ApiException ex = new ApiException(418, "I'm a teapot");
        assertEquals("I'm a teapot", ex.getMessage());
        assertEquals(418, ex.getCode());
    }

    /**
     * NotAuthorizedException should carry status code, message and optional cause.
     */
    @Test
    void notAuthorizedException_holdsStatusCodeAndMessage() {
        NotAuthorizedException ex = new NotAuthorizedException(403, "Forbidden");
        assertEquals("Forbidden", ex.getMessage());
        assertEquals(403, ex.getStatusCode());

        NotAuthorizedException exWithCause =
                new NotAuthorizedException(401, "Unauthorized", new RuntimeException("x"));
        assertEquals("Unauthorized", exWithCause.getMessage());
        assertEquals(401, exWithCause.getStatusCode());
        assertTrue(exWithCause.getCause() instanceof RuntimeException);
    }

    /**
     * ValidationException should carry only a message.
     */
    @Test
    void validationException_messageOnly() {
        ValidationException ex = new ValidationException("Bad data");
        assertEquals("Bad data", ex.getMessage());
    }
}
