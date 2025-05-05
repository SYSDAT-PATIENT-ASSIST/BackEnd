package dk.patientassist.persistence.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testAllergensSerializationDeserialization() throws Exception {
        String json = mapper.writeValueAsString(Allergens.ÆG);
        assertEquals("\"æg\"", json);

        Allergens deserialized = mapper.readValue("\"Æg\"", Allergens.class);
        assertEquals(Allergens.ÆG, deserialized);
    }

    @Test
    void testDishStatusSerializationDeserialization() throws Exception {
        String json = mapper.writeValueAsString(DishStatus.UDSOLGT);
        assertEquals("\"udsolgt\"", json);

        DishStatus deserialized = mapper.readValue("\"udsolgt\"", DishStatus.class);
        assertEquals(DishStatus.UDSOLGT, deserialized);
    }

    @Test
    void testOrderStatusSerializationDeserialization() throws Exception {
        String json = mapper.writeValueAsString(OrderStatus.FÆRDIG);
        assertEquals("\"færdig\"", json);

        OrderStatus deserialized = mapper.readValue("\"FÆRDIG\"", OrderStatus.class);
        assertEquals(OrderStatus.FÆRDIG, deserialized);
    }

    @Test
    void testRoleSerializationDeserialization() throws Exception {
        String json = mapper.writeValueAsString(Role.KOK);
        assertEquals("\"kok\"", json);

        Role deserialized = mapper.readValue("\"KoK\"", Role.class);
        assertEquals(Role.KOK, deserialized);
    }

    @Test
    void testInvalidAllergenThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                Allergens.fromString("chokolade"));
        assertEquals("Ugyldig allergen: chokolade", ex.getMessage());
    }

    @Test
    void testInvalidOrderStatusThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                OrderStatus.fromString("forkert"));
        assertEquals("Ugyldig ordrestatus: forkert", ex.getMessage());
    }
}
