package dk.patientassist.test;

import dk.patientassist.persistence.dto.DishDTO;
import dk.patientassist.persistence.dto.RecipeDTO;
import dk.patientassist.persistence.ent.Dish;
import dk.patientassist.persistence.ent.Recipe;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DishDTO} and {@link RecipeDTO}.
 */
@ExtendWith(MockitoExtension.class)
class DishDTOTest {

    /**
     * Test manual construction of a DishDTO with all fields set.
     */
    @Test
    void testConstructorWithAllFields() {
        LocalDate from = LocalDate.now();
        LocalDate until = from.plusDays(10);
        Set<Allergens> allergens = Set.of(Allergens.ÆG);
        RecipeDTO recipeDTO = new RecipeDTO();

        DishDTO dto = new DishDTO(
                "Frikadeller", "Med sovs", from, until, DishStatus.TILGÆNGELIG,
                500.0, 25.0, 40.0, 20.0, allergens, recipeDTO
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
        assertTrue(dto.getAllergens().contains(Allergens.ÆG));
    }

    /**
     * Test creation of DishDTO from a fully populated Dish entity.
     */
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
        dish.setAllergens(Set.of(Allergens.SULFITTER));

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
        assertTrue(dto.getAllergens().contains(Allergens.SULFITTER));
    }

    /**
     * Test creation of DishDTO from a Dish entity with a mocked Recipe.
     */
    @Test
    void testConstructorFromEntityWithMockedRecipe() {
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
        dish.setAllergens(Set.of(Allergens.SULFITTER));

        // Mock a Recipe entity
        Recipe mockedRecipe = mock(Recipe.class);
        when(mockedRecipe.getTitle()).thenReturn("Persillesovs");
        dish.setRecipe(mockedRecipe);

        DishDTO dto = new DishDTO(dish);

        assertEquals("Stegt flæsk", dto.getName());
        assertEquals("Med persillesovs", dto.getDescription());
        assertEquals(DishStatus.UDSOLGT, dto.getStatus());
        assertTrue(dto.getAllergens().contains(Allergens.SULFITTER));

        assertNotNull(dto.getRecipe());
        assertEquals("Persillesovs", dto.getRecipe().getTitle());
    }

    /**
     * Test creation of RecipeDTO from a mocked Recipe entity.
     */
    @Test
    void testRecipeDTOFromRecipeEntity() {
        Recipe mockedRecipe = mock(Recipe.class);
        when(mockedRecipe.getTitle()).thenReturn("Tomatsuppe");
        when(mockedRecipe.getId()).thenReturn(42);

        RecipeDTO dto = new RecipeDTO(mockedRecipe);

        assertEquals("Tomatsuppe", dto.getTitle());
        assertEquals(42, dto.getId());
    }
}
