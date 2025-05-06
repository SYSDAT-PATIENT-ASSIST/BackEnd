Feature: Menu management by head chef

  As head chef (admin),
  I want to manage the hospital’s digital menu,
  So that patients can order from an up-to-date selection of available meals.

  Background:
    Given I am logged in as head chef
    And I am on the "Menu Management" page

  Scenario: Add a new dish to the menu
    When I click the "Tilføj" button
    And I enter "Kyllingesalat" in the "Titel" field
    And I enter "Let og frisk salat med grillet kylling" in the "Beskrivelse" field
    And I enter "200 kcal, 20g protein" in the "Ernæringsinfo" field
    And I enter "Grillet kylling, salat, olie" in the "Opskrift" field
    And I click the "Gem" button
    Then the system should validate the input
    And the dish "Kyllingesalat" should be added to the menu
    And I should see the message "Retten er blevet tilføjet."

  Scenario: Show error when mandatory fields are missing during add
    When I click the "Tilføj" button
    And I leave the "Titel" field empty
    And I click the "Gem" button
    Then the system should highlight the "Titel" field
    And I should see the error message "Udfyld venligst alle obligatoriske felter"

  Scenario: Remove a dish from the menu
    Given the dish "Spaghetti Bolognese" exists in the menu
    When I click on "Spaghetti Bolognese"
    And I click the "Fjern" button
    And I confirm the removal in the dialog
    Then the dish "Spaghetti Bolognese" should be removed from the menu
    And I should see the message "Retten er blevet fjernet."
    And "Spaghetti Bolognese" should no longer be visible in the menu list

  Scenario: Edit an existing dish
    Given the dish "Grøntsagssuppe" exists in the menu
    When I click on "Grøntsagssuppe"
    And I click the "Rediger" button
    And I change the "Titel" to "Krydret Grøntsagssuppe"
    And I click the "Gem" button
    Then the system should validate the updated input
    And the dish title should be updated to "Krydret Grøntsagssuppe"
    And I should see the message "Ændringer er gemt."

  Scenario: Show error when mandatory fields are missing during edit
    Given the dish "Frugtsalat" exists in the menu
    When I click on "Frugtsalat"
    And I click the "Rediger" button
    And I clear the "Titel" field
    And I click the "Gem" button
    Then the system should highlight the "Titel" field
    And I should see the error message "Udfyld venligst alle obligatoriske felter"
