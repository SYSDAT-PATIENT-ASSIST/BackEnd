Feature: Dish CRUD operations

  Scenario: Create a new dish
    Given I have a new dish with name "Frikadeller"
    When I send a POST request to "/dishes"
    Then I receive a 201 Created response
    And the dish is available when I fetch all dishes

  Scenario: Update dish name
    When I update the dish name to "Frikadeller m. sovs"
    Then the dish name is updated successfully

  Scenario: Replace dish details using PUT
    When I replace the dish details using PUT
    Then the dish is replaced successfully

  Scenario: Delete the dish
    When I delete the dish
    Then the dish is deleted successfully
    And the dish no longer exists
