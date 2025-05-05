Feature: Public Dish API

  As a system user
  I want to interact with the dish API
  So that I can view and manage available dishes

  Background:
    Given I am using the public Dish API

  Scenario: Retrieve all available dishes
    When I GET "/api/dishes"
    Then the response status should be 200
    And the response should contain a list of dishes

  Scenario: Retrieve a dish by ID
    Given a dish named "Æbleflæsk" exists
    When I GET "/api/dishes/{id}" for that dish
    Then the response status should be 200
    And the dish name should be "Æbleflæsk"

  Scenario: Create a new dish via API
    Given the following new dish:
      | name        | description     | status       | kcal | protein | carbohydrates | fat | allergens |
      | Karrysuppe  | Mild suppe      | TILGÆNGELIG  | 300  | 10      | 25            | 8   | LAKTOSE   |
    When I POST the dish to "/api/dishes"
    Then the response status should be 201
    And the dish name should be "Karrysuppe"

  Scenario: Delete an existing dish
    Given a dish named "Karrysuppe" exists
    When I DELETE "/api/dishes/{id}" for that dish
    Then the response status should be 200
    And the dish "Karrysuppe" should no longer exist

  Scenario: Patch dish name
    Given a dish named "Rugbrødsmad" exists
    When I PATCH the field "name" with value "Opdateret Rugbrød" on that dish
    Then the response status should be 200
    And the dish name should be "Opdateret Rugbrød"
