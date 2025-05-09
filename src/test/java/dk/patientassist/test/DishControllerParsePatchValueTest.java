package dk.patientassist.test;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DishControllerParsePatchValueTest {

    private final DishController controller = new DishController(null);

    @Test
    void parsePatchValue_validDoubleFields() {
        assertEquals(123.45, invoke("kcal", "123.45"));
        assertEquals(5.0, invoke("protein", "5"));
        assertEquals(0.0, invoke("fat", "0"));
    }

    @Test
    void parsePatchValue_validEnumFields() {
        assertEquals(DishStatus.UDSOLGT, invoke("status", "UDSOLGT"));
        assertEquals(Allergens.SESAM, invoke("allergens", "SESAM"));
    }

    @Test
    void parsePatchValue_validDateFields() {
        assertEquals(LocalDate.of(2024, 1, 1), invoke("availableFrom", "2024-01-01"));
        assertEquals(LocalDate.of(2030, 12, 31), invoke("availableUntil", "2030-12-31"));
    }

    @Test
    void parsePatchValue_validStringFields() {
        assertEquals("Frikadeller", invoke("name", "Frikadeller"));
        assertEquals("Med sovs", invoke("description", "Med sovs"));
    }

    @Test
    void parsePatchValue_invalidDouble_throws() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> invoke("kcal", "notANumber"));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertInstanceOf(NumberFormatException.class, thrown.getCause().getCause());
    }

    @Test
    void parsePatchValue_invalidEnum_throws() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> invoke("status", "INVALID_ENUM"));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("Ugyldig dish status"));
    }

    @Test
    void parsePatchValue_invalidAllergen_throws() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> invoke("allergens", "NOPE"));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("Ugyldig allergen"));
    }

    @Test
    void parsePatchValue_invalidDate_throws() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> invoke("availableFrom", "not-a-date"));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertInstanceOf(java.time.format.DateTimeParseException.class, thrown.getCause().getCause());
    }

    @Test
    void parsePatchValue_unknownField_defaultsToString() {
        assertEquals("somevalue", invoke("customField", "somevalue"));
    }

    /**
     * Helper method to invoke private parsePatchValue using reflection.
     */
    private Object invoke(String field, String value) {
        try {
            Method method = DishController.class.getDeclaredMethod("parsePatchValue", String.class, String.class);
            method.setAccessible(true);
            return method.invoke(controller, field, value);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
        }
    }
}
