package dk.patientassist.test;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DishDTOTest {

    @Test
    void testConstructorWithAllFields() {
        LocalDate from = LocalDate.now();
        LocalDate until = from.plusDays(10);

        DishDTO dto = new DishDTO(
                "Frikadeller", "Med sovs", from, until, DishStatus.TILGÆNGELIG,
                500.0, 25.0, 40.0, 20.0, Allergens.ÆG
        );

        assertEquals("Frikadeller", dto.getName());
        assertEquals("Med sovs", dto.getDescription());
        assertEquals(from, dto.getAvailableFrom());
        assertEquals(until, dto.getAvailableUntil());
        assertEquals(DishStatus.TILGÆNGELIG, dto.getStatus());
        assertEquals(500.0, dto.getKcal());
        assertEquals(25.0, dto.getProtein());
        assertEquals(40.0, dto.getCarbohydrates());
        assertEquals(20.0, dto.getFat());
        assertEquals(Allergens.ÆG, dto.getAllergens());
    }

    @Test
    void testConstructorFromEntity() {
        Dish dish = new Dish();
        dish.setName("Stegt flæsk");
        dish.setDescription("Med persillesovs");
        dish.setAvailableFrom(LocalDate.of(2024, 1, 1));
        dish.setAvailableUntil(LocalDate.of(2024, 1, 10));
        dish.setStatus(DishStatus.UDSOLGT);
        dish.setKcal(600.0);
        dish.setProtein(30.0);
        dish.setCarbohydrates(50.0);
        dish.setFat(25.0);
        dish.setAllergens(Allergens.SULFITTER);

        DishDTO dto = new DishDTO(dish);

        assertEquals("Stegt flæsk", dto.getName());
        assertEquals("Med persillesovs", dto.getDescription());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getAvailableFrom());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getAvailableUntil());
        assertEquals(DishStatus.UDSOLGT, dto.getStatus());
        assertEquals(600.0, dto.getKcal());
        assertEquals(30.0, dto.getProtein());
        assertEquals(50.0, dto.getCarbohydrates());
        assertEquals(25.0, dto.getFat());
        assertEquals(Allergens.SULFITTER, dto.getAllergens());
    }
}
