package dk.patientassist.persistence.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DishDTOSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSerializeDishDTO() throws Exception {
        DishDTO dto = new DishDTO(
                "Frikadeller",
                "Dansk klassiker",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                DishStatus.TILGÆNGELIG,
                550.0,
                25.0,
                30.0,
                22.0,
                Allergens.GLUTEN
        );

        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("\"status\":\"tilgængelig\""));
        assertTrue(json.contains("\"allergens\":\"gluten\""));
        assertTrue(json.contains("\"availableFrom\":\"2025-05-01\""));
        assertTrue(json.contains("\"name\":\"Frikadeller\""));
    }

    @Test
    void testDeserializeDishDTO() throws Exception {
        String json = """
            {
              "name": "Stegt flæsk",
              "description": "Serveres med persillesovs",
              "availableFrom": "2025-04-01",
              "availableUntil": "2025-04-30",
              "status": "udsolgt",
              "kcal": 750.0,
              "protein": 35.0,
              "carbohydrates": 20.0,
              "fat": 45.0,
              "allergens": "æg"
            }
        """;

        DishDTO dto = mapper.readValue(json, DishDTO.class);

        assertEquals("Stegt flæsk", dto.getName());
        assertEquals(DishStatus.UDSOLGT, dto.getStatus());
        assertEquals(Allergens.ÆG, dto.getAllergens());
        assertEquals(LocalDate.of(2025, 4, 1), dto.getAvailableFrom());
    }
}
