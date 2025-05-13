Feature: Menu management by head chef

  As head chef
  I want to manage the hospital’s digital menu
  So that patients can order from an up-to-date selection of available meals

  Background:
    Given I am logged in as head chef (admin)
    And I am on the "Menu Management" page

  Scenario: Add new dish to menu
    When I click the "Tilføj" button
    And I enter all required dish details:
      | titel        | Frikadeller              |
      | beskrivelse  | Med kartofler og sovs    |
      | ernæringsinfo| 500 kcal, 20g protein    |
      | opskrift     | steg i ovn i 20 min      |
    And I click "Gem" to save the dish
    Then the system validates the input
    And ensures all mandatory fields are filled

    # If validation passes:
    And the system adds the dish to the menu
    And a confirmation message is displayed: "Retten er blevet tilføjet."

  Scenario: Remove dish from menu
    When I locate and click on the dish "Frikadeller"
    And I click the "Fjern" button
    And I confirm the removal in a pop-up dialog: "Er du sikker på, at du vil fjerne denne ret?"
    Then the system removes the dish from the menu
    And a confirmation message is displayed: "Retten er blevet fjernet."
    And the dish "Frikadeller" no longer appears in the menu list

  Scenario: Edit an existing dish
    When I locate and click on the dish "Frikadeller"
    And I click the "Rediger" button
    And I modify the dish details:
      | titel        | Frikadeller med løg        |
      | beskrivelse  | Nu med karamelliserede løg |
    And I click "Gem" to save the changes
    Then the system validates the updated input
    And ensures all mandatory fields are still filled
    And checks for invalid characters or formatting

    # If validation passes:
    And the system saves the changes
    And a confirmation message is displayed: "Ændringer er gemt."
