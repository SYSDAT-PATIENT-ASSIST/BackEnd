Feature: Dish API Management

  As a client of the Dish API,
  I want to be able to create, update, replace, and delete dishes,
  So that I can manage menu items effectively.

  Scenario: Create a new dish
    Given I have a new dish with name "Boller i karry"
    When I send a POST request to "/dishes"
    Then I receive a 201 Created response
    And the dish is available when I fetch all dishes

  Scenario: Update the dish name using PATCH
    Given I have a new dish with name "Stegt flæsk"
    When I send a POST request to "/dishes"
    And I update the dish name to "Stegt flæsk med persillesovs"
    Then the dish name is updated successfully

  Scenario: Replace dish using PUT
    Given I have a new dish with name "Rugbrød med leverpostej"
    When I send a POST request to "/dishes"
    And I replace the dish details using PUT
    Then the dish is replaced successfully

  Scenario: Delete a dish
    Given I have a new dish with name "Pølsemix"
    When I send a POST request to "/dishes"
    And I send a DELETE request to "/dishes/{int}"
    Then the dish is deleted successfully
    And the dish no longer exists
