package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishDTOTest {

    private DishDTO dish;

    @BeforeEach
    void setUp() {
        dish = new DishDTO(
                "Boller i karry",
                "Klassisk dansk ret med svinekød og karrysauce",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                DishStatus.TILGÆNGELIG,
                580.0,
                22.0,
                48.0,
                18.0,
                Allergens.GLUTEN
        );
        dish.setId(1);
    }

    // --- Getter tests ---
    @Test void getId() { assertEquals(1, dish.getId()); }
    @Test void getName() { assertEquals("Boller i karry", dish.getName()); }
    @Test void getDescription() { assertEquals("Klassisk dansk ret med svinekød og karrysauce", dish.getDescription()); }
    @Test void getAvailable_from() { assertEquals(LocalDate.of(2025, 5, 1), dish.getAvailable_from()); }
    @Test void getAvailable_until() { assertEquals(LocalDate.of(2025, 6, 1), dish.getAvailable_until()); }
    @Test void getStatus() { assertEquals(DishStatus.TILGÆNGELIG, dish.getStatus()); }
    @Test void getKcal() { assertEquals(580.0, dish.getKcal()); }
    @Test void getProtein() { assertEquals(22.0, dish.getProtein()); }
    @Test void getCarbohydrates() { assertEquals(48.0, dish.getCarbohydrates()); }
    @Test void getFat() { assertEquals(18.0, dish.getFat()); }
    @Test void getAllergens() { assertEquals(Allergens.GLUTEN, dish.getAllergens()); }

    // --- Setter tests ---
    @Test void setId() { dish.setId(99); assertEquals(99, dish.getId()); }
    @Test void setName() { dish.setName("Stegt flæsk"); assertEquals("Stegt flæsk", dish.getName()); }
    @Test void setDescription() {
        dish.setDescription("Danmarks nationalret med persillesovs");
        assertEquals("Danmarks nationalret med persillesovs", dish.getDescription());
    }
    @Test void setAvailable_from() {
        LocalDate d = LocalDate.now(); dish.setAvailable_from(d); assertEquals(d, dish.getAvailable_from());
    }
    @Test void setAvailable_until() {
        LocalDate d = LocalDate.now(); dish.setAvailable_until(d); assertEquals(d, dish.getAvailable_until());
    }
    @Test void setStatus() {
        dish.setStatus(DishStatus.UDGÅET); assertEquals(DishStatus.UDGÅET, dish.getStatus());
    }
    @Test void setKcal() { dish.setKcal(700.0); assertEquals(700.0, dish.getKcal()); }
    @Test void setProtein() { dish.setProtein(30.0); assertEquals(30.0, dish.getProtein()); }
    @Test void setCarbohydrates() { dish.setCarbohydrates(40.0); assertEquals(40.0, dish.getCarbohydrates()); }
    @Test void setFat() { dish.setFat(20.0); assertEquals(20.0, dish.getFat()); }
    @Test void setAllergens() { dish.setAllergens(Allergens.LAKTOSE); assertEquals(Allergens.LAKTOSE, dish.getAllergens()); }

    // --- toString ---
    @Test void testToString() {
        String result = dish.toString();
        assertNotNull(result);
        assertTrue(result.contains("Boller i karry"));
    }

    // --- Constructor validation tests ---
    @Test void constructor_throws_if_name_is_null_or_blank() {
        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                null, "desc", LocalDate.now(), LocalDate.now(), DishStatus.TILGÆNGELIG,
                100, 10, 20, 5, Allergens.GLUTEN));

        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                " ", "desc", LocalDate.now(), LocalDate.now(), DishStatus.TILGÆNGELIG,
                100, 10, 20, 5, Allergens.GLUTEN));
    }

    @Test void constructor_throws_if_description_is_null() {
        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                "Test", null, LocalDate.now(), LocalDate.now(), DishStatus.TILGÆNGELIG,
                100, 10, 20, 5, Allergens.GLUTEN));
    }

    @Test void constructor_throws_if_dates_invalid() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        LocalDate until = LocalDate.of(2025, 5, 1);
        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                "Test", "desc", from, until, DishStatus.TILGÆNGELIG,
                100, 10, 20, 5, Allergens.GLUTEN));
    }

    @Test void constructor_throws_if_status_or_allergens_is_null() {
        LocalDate from = LocalDate.now(), until = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                "Test", "desc", from, until, null,
                100, 10, 20, 5, Allergens.GLUTEN));

        assertThrows(IllegalArgumentException.class, () -> new DishDTO(
                "Test", "desc", from, until, DishStatus.TILGÆNGELIG,
                100, 10, 20, 5, null));
    }

    // --- Entity constructor test ---
    @Test
    void dishConstructor_copies_all_fields_correctly() {
        Dish mockDish = mock(Dish.class);
        when(mockDish.getId()).thenReturn(1);
        when(mockDish.getName()).thenReturn("Grønkålssuppe");
        when(mockDish.getDescription()).thenReturn("Varm suppe med grønkål og kartofler");
        when(mockDish.getAvailable_from()).thenReturn(LocalDate.of(2025, 1, 1));
        when(mockDish.getAvailable_until()).thenReturn(LocalDate.of(2025, 2, 1));
        when(mockDish.getStatus()).thenReturn(DishStatus.TILGÆNGELIG);
        when(mockDish.getKcal()).thenReturn(350.0);
        when(mockDish.getProtein()).thenReturn(15.0);
        when(mockDish.getCarbohydrates()).thenReturn(30.0);
        when(mockDish.getFat()).thenReturn(10.0);
        when(mockDish.getAllergens()).thenReturn(Allergens.SELLERI);

        DishDTO dto = new DishDTO(mockDish);

        assertEquals("Grønkålssuppe", dto.getName());
        assertEquals("Varm suppe med grønkål og kartofler", dto.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getAvailable_from());
        assertEquals(LocalDate.of(2025, 2, 1), dto.getAvailable_until());
        assertEquals(DishStatus.TILGÆNGELIG, dto.getStatus());
        assertEquals(350.0, dto.getKcal());
        assertEquals(15.0, dto.getProtein());
        assertEquals(30.0, dto.getCarbohydrates());
        assertEquals(10.0, dto.getFat());
        assertEquals(Allergens.SELLERI, dto.getAllergens());
    }
}
