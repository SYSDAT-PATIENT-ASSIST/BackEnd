package dk.patientassist.test.persistence.dto;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Recipe;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DishDTOTest {

    private static final Logger log = LoggerFactory.getLogger(DishDTOTest.class);

    @Test
    void testFullArgsConstructor() {
        log.info("Running testFullArgsConstructor");

        LocalDate from = LocalDate.now();
        LocalDate until = from.plusDays(7);
        Set<Allergens> allergens = EnumSet.of(Allergens.GLUTEN, Allergens.LAKTOSE);
        RecipeDTO recipeDTO = new RecipeDTO();

        DishDTO dto = new DishDTO(
                "Frikadeller",
                "Klassiske danske frikadeller med kartofler og brun sovs",
                from,
                until,
                DishStatus.TILGÆNGELIG,
                650.0,
                30.0,
                40.0,
                35.0,
                allergens,
                recipeDTO
        );

        log.debug("Constructed DTO: {}", dto);

        assertThat(dto.getName(), is("Frikadeller"));
        assertThat(dto.getDescription(), containsString("frikadeller"));
        assertThat(dto.getAvailableFrom(), is(from));
        assertThat(dto.getAvailableUntil(), is(until));
        assertThat(dto.getStatus(), is(DishStatus.TILGÆNGELIG));
        assertThat(dto.getKcal(), is(650.0));
        assertThat(dto.getProtein(), is(30.0));
        assertThat(dto.getCarbohydrates(), is(40.0));
        assertThat(dto.getFat(), is(35.0));
        assertThat(dto.getAllergens(), containsInAnyOrder(Allergens.GLUTEN, Allergens.LAKTOSE));
        assertThat(dto.getRecipe(), is(recipeDTO));
    }

    @Test
    void testEntityConstructor() {
        log.info("Running testEntityConstructor");

        Dish dish = new Dish();
        dish.setId(5);
        dish.setName("Smørrebrød");
        dish.setDescription("Rugbrød med leverpostej og agurk");
        dish.setAvailableFrom(LocalDate.of(2025, 5, 1));
        dish.setAvailableUntil(LocalDate.of(2025, 5, 15));
        dish.setStatus(DishStatus.UDGÅET);
        dish.setKcal(480);
        dish.setProtein(18);
        dish.setCarbohydrates(45);
        dish.setFat(22);
        dish.setAllergens(EnumSet.of(Allergens.GLUTEN));
        dish.setRecipe(new Recipe());

        DishDTO dto = new DishDTO(dish);

        log.debug("Mapped DTO from entity: {}", dto);

        assertThat(dto.getId(), is(5));
        assertThat(dto.getName(), is("Smørrebrød"));
        assertThat(dto.getDescription(), containsString("leverpostej"));
        assertThat(dto.getAvailableFrom(), is(LocalDate.of(2025, 5, 1)));
        assertThat(dto.getAvailableUntil(), is(LocalDate.of(2025, 5, 15)));
        assertThat(dto.getStatus(), is(DishStatus.UDGÅET));
        assertThat(dto.getKcal(), is(480.0));
        assertThat(dto.getProtein(), is(18.0));
        assertThat(dto.getCarbohydrates(), is(45.0));
        assertThat(dto.getFat(), is(22.0));
        assertThat(dto.getAllergens(), contains(Allergens.GLUTEN));
        assertThat(dto.getRecipe(), is(notNullValue()));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        log.info("Running testNoArgsConstructorAndSetters");

        DishDTO dto = new DishDTO();
        dto.setId(10);
        dto.setName("Rødgrød med fløde");
        dto.setDescription("En klassisk dansk dessert med bær og fløde");

        log.debug("DTO after setting fields: {}", dto);

        assertThat(dto.getId(), is(10));
        assertThat(dto.getName(), is("Rødgrød med fløde"));
        assertThat(dto.getDescription(), containsString("dansk dessert"));
    }
}
