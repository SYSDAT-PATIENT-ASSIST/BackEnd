Feature: Kitchen Staff Meal Order Management
  As kitchen staff
  I want to efficiently receive and manage patient meal orders
  So that meals are prepared and delivered correctly and on time.

  Scenario: Viewing new meal orders
    Given a patient has placed a meal order
    When the order is received in the kitchen system
    Then the kitchen staff can see the patient’s bed number, selected dishes, and any dietary comments.

  Scenario: Accepting and preparing a meal order
    Given a “Hakkebøf” order is visible in the kitchen system
    When the kitchen staff accepts the order
    Then the system updates the order status
    And the kitchen staff can start preparing the meal.

  Scenario: Notifying the patient that the meal is on the way
    Given the meal order is being prepared
    When the kitchen staff marks the order as "Klar til levering"
    Then the system notifies the patient with the message "Din mad er på vej".

  Scenario: Handling a delayed meal order
    Given a meal order is delayed due to unforeseen circumstances
    When the kitchen staff updates the system with a delay
    Then the system notifies the patient with the message "Din mad er forsinket."
