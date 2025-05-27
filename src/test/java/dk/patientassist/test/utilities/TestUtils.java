package dk.patientassist.test.utilities;

import dk.patientassist.service.dto.DishDTO;
import dk.patientassist.service.dto.IngredientDTO;
import dk.patientassist.service.dto.RecipeDTO;
import dk.patientassist.persistence.enums.Allergens;
import dk.patientassist.persistence.enums.DishStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for building mock DTOs for unit tests.
 */
public class TestUtils {

    /**
     * Builds a RecipeDTO from title, instructions and ingredient names.
     *
     * @param title           Recipe title
     * @param instructions    Cooking instructions
     * @param ingredientNames List of ingredient names
     * @return a populated RecipeDTO
     */
    public RecipeDTO buildRecipeDTO(String title, String instructions, List<String> ingredientNames) {
        List<IngredientDTO> ingredientDTOs = ingredientNames.stream()
                .map(IngredientDTO::new)
                .collect(Collectors.toList());

        return new RecipeDTO(title, instructions, ingredientDTOs);
    }

    /**
     * Builds a simple DishDTO with default status and allergens.
     *
     * @param name Name of the dish
     * @param kcal Energy content in kcal
     * @return a fully populated DishDTO
     */
    public DishDTO buildDishDTO(String name, double kcal) {
        return buildDishDTO(
                name,
                DishStatus.TILGÆNGELIG,
                Set.of(Allergens.GLUTEN)
        );
    }

    /**
     * Builds a DishDTO with custom status and allergens.
     *
     * @param name      Name of the dish
     * @param status    Dish availability status
     * @param allergens Set of allergens
     * @return a fully populated DishDTO
     */
    public DishDTO buildDishDTO(String name, DishStatus status, Set<Allergens> allergens) {
        RecipeDTO recipe = buildRecipeDTO(
                "Opskrift til " + name,
                "Bland ingredienser og tilbered",
                List.of("Salt", "Peber", "Kylling")
        );

        return new DishDTO(
                name,
                "Testbeskrivelse for " + name,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                status,
                300,
                25,
                40,
                10,
                allergens,
                recipe
        );
    }
}
