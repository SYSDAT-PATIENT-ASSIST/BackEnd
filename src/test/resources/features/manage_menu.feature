Feature: Manage the hospital's digital menu

  As a head chef
  I want to manage the hospital’s digital menu
  So that patients can order from an up-to-date selection of available meals

  Background:
    Given I am logged in as head chef

  Scenario: Add a new dish to the menu
    Given I am on the "Menu Management" page
    When I click the "Tilføj" button
    And I enter the following dish details:
      | name        | description         | kcal | protein | carbohydrates | fat | status      | allergens | recipeTitle             |
      | Frikadeller | Klassisk ret        | 600  | 30      | 40            | 20  | TILGÆNGELIG | GLUTEN    | Frikadeller med sovs    |
    And I click the "Gem" button
    Then the dish is saved
    And a confirmation message "Retten er blevet tilføjet." is displayed

  Scenario: Remove a dish from the menu
    Given I am on the "Menu Management" page
    When I select the dish named "Frikadeller"
    And I click the "Fjern" button
    And I confirm the removal
    Then the dish is deleted
    And a confirmation message "Retten er blevet fjernet." is displayed
    And the dish "Frikadeller" is no longer in the menu

  Scenario: Edit an existing dish
    Given I am on the "Menu Management" page
    When I select the dish named "Frikadeller"
    And I click the "Rediger" button
    And I update the dish details:
      | field       | value                  |
      | description | Opdateret beskrivelse  |
      | kcal        | 550                    |
    And I click the "Gem" button
    Then the dish is updated successfully
    And a confirmation message "Ændringer er gemt." is displayed

  Scenario: Update only one field (fat)
    Given I am on the "Menu Management" page
    When I patch the field "fat" of "Frikadeller" with value "19"
    Then the dish "Frikadeller" should have field "fat" equal to "19.0"
