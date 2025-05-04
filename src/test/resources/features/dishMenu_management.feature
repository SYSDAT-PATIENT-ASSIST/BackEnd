Feature: Dish Menu Management by Head Chef

  As a head chef,
  I want to manage the hospital’s digital menu,
  So that patients can order from an up-to-date selection of available meals.

  Scenario: Add new dish to dish menu
    Given I am logged in as a head chef
    And I am on the "Dish Menu Management" page
    When I click the "Tilføj" button
    And I enter all required dish details
    And I add description, nutrition info, and recipe
    And I click "Gem" to save the dish
    Then the system validates the input
    And the system adds the dish to the dish menu
    And a confirmation message is displayed: "Retten er blevet tilføjet"

  Scenario: Remove dish from menu
    Given I am logged in as a head chef
    And I am on the "Dish Menu Management" page
    And a dish titled "Øllebrød" exists in the menu
    When I click on the dish "Øllebrød"
    And I click the "Fjern" button
    And I confirm the removal
    Then the system removes the dish from the dish menu
    And a confirmation message is displayed: "Retten er blevet fjernet"
    And the dish "Øllebrød" no longer appears in the menu list

  Scenario: Edit the dish
    Given I am logged in as a head chef
    And I am on the "Dish Menu Management" page
    And a dish titled "Frikadeller" exists in the menu
    When I click on the dish "Frikadeller"
    And I click the "Rediger" button
    And I change the title to "Frikadeller m. sovs"
    And I optionally update the description or nutrition info
    And I click "Gem" to save the changes
    Then the system validates the updated input
    And the system saves the changes
    And a confirmation message is displayed: "Ændringer er gemt."
