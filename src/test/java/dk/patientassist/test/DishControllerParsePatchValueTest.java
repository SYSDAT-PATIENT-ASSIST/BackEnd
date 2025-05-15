package dk.patientassist.test;

import dk.patientassist.control.DishController;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;

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
        assertEquals(LocalDate.of(2024, 1, 1), invoke("available_from", "2024-01-01"));
        assertEquals(LocalDate.of(2030, 12, 31), invoke("available_until", "2030-12-31"));
    }

    @Test
    void parsePatchValue_validStringFields() {
        assertEquals("Frikadeller", invoke("name", "Frikadeller"));
        assertEquals("Med sovs", invoke("description", "Med sovs"));
    }

    @Test
    void parsePatchValue_invalidDouble_throws() {
        assertThrows(NumberFormatException.class, () -> invoke("kcal", "notANumber"));
    }

    @Test
    void parsePatchValue_invalidEnum_throws() {
        assertThrows(IllegalArgumentException.class, () -> invoke("status", "INVALID_ENUM"));
        assertThrows(IllegalArgumentException.class, () -> invoke("allergens", "NOPE"));
    }

    @Test
    void parsePatchValue_invalidDate_throws() {
        assertThrows(Exception.class, () -> invoke("available_from", "not-a-date"));
    }

    @Test
    void parsePatchValue_unknownField_defaultsToString() {
        assertEquals("somevalue", invoke("customField", "somevalue"));
    }

    private Object invoke(String field, String value) {
        try {
            var method = DishController.class.getDeclaredMethod("parsePatchValue", String.class, String.class);
            method.setAccessible(true);
            return method.invoke(controller, field, value);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
        }
    }
}
