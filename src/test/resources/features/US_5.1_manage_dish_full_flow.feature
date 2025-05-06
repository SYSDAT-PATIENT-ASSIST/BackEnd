Feature: Full dish management flow

  As a head chef
  I want to manage dishes via the admin system
  So that I can ensure the hospital menu is up-to-date

  Background:
    Given I am logged in as head chef
    And I am on the "Menu Management" page

  Scenario: Add, edit, patch, and delete a dish
    When I click the "Tilføj" button
    And I enter the following dish details:
      | name        | description         | kcal | protein | carbohydrates | fat | status      | allergens | recipeTitle             |
      | Frikadeller | Klassisk ret        | 600  | 30      | 40            | 20  | TILGÆNGELIG | GLUTEN    | Frikadeller med sovs    |
    And I click the "Gem" button
    Then the dish is saved
    And a confirmation message "Retten er blevet tilføjet." is displayed

    When I select the dish named "Frikadeller"
    And I click the "Rediger" button
    And I update the dish details:
      | field       | value                 |
      | description | Opdateret beskrivelse |
      | kcal        | 550                   |
    And I click the "Gem" button
    Then the dish is updated successfully
    And a confirmation message "Ændringer er gemt." is displayed

    When I patch the field "fat" of "Frikadeller" with value "19"
    Then the dish "Frikadeller" should have field "fat" equal to "19.0"

    When I click the "Fjern" button
    And I confirm the removal
    Then the dish is deleted
    And a confirmation message "Retten er blevet fjernet." is displayed
    And the dish "Frikadeller" is no longer in the menu
