package dk.patientassist.test.persistence.entities;

import dk.patientassist.service.dto.DishDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DishTest {

    private static final Logger log = LoggerFactory.getLogger(DishTest.class);

    @Test
    void testConstructorFromDTO_validInput() {
        log.info("Running testConstructorFromDTO_validInput");

        DishDTO dto = new DishDTO();
        dto.setName("Frikadeller");
        dto.setDescription("Stegte frikadeller med kartofler");
        dto.setAvailableFrom(LocalDate.of(2025, 5, 1));
        dto.setAvailableUntil(LocalDate.of(2025, 5, 7));
        dto.setStatus(DishStatus.TILGÆNGELIG);
        dto.setKcal(600);
        dto.setProtein(25);
        dto.setCarbohydrates(30);
        dto.setFat(20);
        dto.setAllergens(Set.of(Allergens.GLUTEN, Allergens.ÆG));

        Dish dish = new Dish(dto);
        log.debug("Created Dish: {}", dish);

        assertThat(dish.getName(), is("Frikadeller"));
        assertThat(dish.getDescription(), containsString("frikadeller"));
        assertThat(dish.getAvailableFrom(), is(LocalDate.of(2025, 5, 1)));
        assertThat(dish.getAvailableUntil(), is(LocalDate.of(2025, 5, 7)));
        assertThat(dish.getStatus(), is(DishStatus.TILGÆNGELIG));
        assertThat(dish.getKcal(), is(600.0));
        assertThat(dish.getProtein(), is(25.0));
        assertThat(dish.getCarbohydrates(), is(30.0));
        assertThat(dish.getFat(), is(20.0));
        assertThat(dish.getAllergens(), hasItems(Allergens.GLUTEN, Allergens.ÆG));
    }

    @Test
    void testConstructorFromDTO_nullDTO_throwsException() {
        log.info("Running testConstructorFromDTO_nullDTO_throwsException");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Dish(null));
        assertThat(ex.getMessage(), is("DishDTO cannot be null"));
        log.debug("Exception: {}", ex.getMessage());
    }

    @Test
    void testConstructorFromDTO_blankName_throwsException() {
        log.info("Running testConstructorFromDTO_blankName_throwsException");

        DishDTO dto = new DishDTO();
        dto.setName("  ");
        dto.setDescription("Test");
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(1));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Dish(dto));
        assertThat(ex.getMessage(), is("Dish name cannot be null or blank"));
    }

    @Test
    void testConstructorFromDTO_nullDescription_throwsException() {
        log.info("Running testConstructorFromDTO_nullDescription_throwsException");

        DishDTO dto = new DishDTO();
        dto.setName("Name");
        dto.setDescription(null);
        dto.setAvailableFrom(LocalDate.now());
        dto.setAvailableUntil(LocalDate.now().plusDays(1));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Dish(dto));
        assertThat(ex.getMessage(), is("Dish description cannot be null"));
    }

    @Test
    void testConstructorFromDTO_invalidDateRange_throwsException() {
        log.info("Running testConstructorFromDTO_invalidDateRange_throwsException");

        DishDTO dto = new DishDTO();
        dto.setName("Name");
        dto.setDescription("Desc");
        dto.setAvailableFrom(LocalDate.of(2025, 5, 10));
        dto.setAvailableUntil(LocalDate.of(2025, 5, 5));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Dish(dto));
        assertThat(ex.getMessage(), is("Available from date cannot be after available until date"));
    }

    @Test
    void testConstructorFromDTO_nullDates_throwsException() {
        log.info("Running testConstructorFromDTO_nullDates_throwsException");

        DishDTO dto = new DishDTO();
        dto.setName("Name");
        dto.setDescription("Desc");
        dto.setAvailableFrom(null);
        dto.setAvailableUntil(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Dish(dto));
        assertThat(ex.getMessage(), is("Available from/until dates cannot be null"));
    }

    @Test
    void testGettersAndSetters() {
        log.info("Running testGettersAndSetters");

        Dish dish = new Dish();
        dish.setId(1);
        dish.setName("Smørrebrød");
        dish.setDescription("Rugbrød med leverpostej");
        dish.setAvailableFrom(LocalDate.of(2025, 6, 1));
        dish.setAvailableUntil(LocalDate.of(2025, 6, 10));
        dish.setStatus(DishStatus.UDGÅET);
        dish.setKcal(450);
        dish.setProtein(15);
        dish.setCarbohydrates(55);
        dish.setFat(18);
        dish.setAllergens(Set.of(Allergens.LAKTOSE));

        assertThat(dish.getId(), is(1));
        assertThat(dish.getName(), is("Smørrebrød"));
        assertThat(dish.getDescription(), is("Rugbrød med leverpostej"));
        assertThat(dish.getStatus(), is(DishStatus.UDGÅET));
        assertThat(dish.getAvailableFrom(), is(LocalDate.of(2025, 6, 1)));
        assertThat(dish.getAvailableUntil(), is(LocalDate.of(2025, 6, 10)));
        assertThat(dish.getKcal(), is(450.0));
        assertThat(dish.getProtein(), is(15.0));
        assertThat(dish.getCarbohydrates(), is(55.0));
        assertThat(dish.getFat(), is(18.0));
        assertThat(dish.getAllergens(), containsInAnyOrder(Allergens.LAKTOSE));
    }
}
