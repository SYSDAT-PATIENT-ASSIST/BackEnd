Feature:
  As a patient, I want to order food using the iPad, so that I can receive meals based on my preferences.

  Scenario: Order dish on the Menu
    Given the patient has an assigned bed number and an iPad
    And the patient has opened the Menu on the iPad
    When the patient selects a dish from the Menu
    And the patient clicks the "Bestil" button
    Then a confirmation message should be displayed on the screen.

  Scenario: Cancel an order:
    Given the patient has placed an order
    And the patient regrets the order or needs to make changes to it
    When the patient chooses to cancel the order before the deadline
    And the patient presses the "Afbestil" button
    Then the order will be cancelled
    And the system will be updated.

  Scenario: Order something that is sold out:
    Given a dish is sold out
    When the patient views the list of available dishes
    Then it should be clearly indicated (e.g. with a “Sold Out” label) that the dish cannot be ordered

