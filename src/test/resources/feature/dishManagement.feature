Feature: Dish management

  Scenario: Create a new valid dish
    When I create a dish with name "Vegetarian Soup" and status "AVAILABLE"
    Then the response status should be 201
    And the dish name should be "Vegetarian Soup"

  Scenario: Update dish status to SOLD_OUT
    When I update dish "Vegetarian Soup" to status "SOLD_OUT"
    Then the updated dish status should be "SOLD_OUT"

  Scenario: Fail to create dish with invalid status
    When I try to create a dish with status "INVALID_STATUS"
    Then the response status should be 400

  Scenario: Get list of available dishes
    When I fetch all available dishes
    Then the list should contain at least one dish

  Scenario: Filter dishes by status and allergen
    When I fetch dishes filtered by status "AVAILABLE" and allergen "GLUTEN"
    Then the response status should be 200
    And the list should contain only dishes with status "AVAILABLE" and allergen "GLUTEN"
