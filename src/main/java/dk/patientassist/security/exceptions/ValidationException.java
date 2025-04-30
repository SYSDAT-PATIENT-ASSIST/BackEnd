package dk.patientassist.security.exceptions;

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}