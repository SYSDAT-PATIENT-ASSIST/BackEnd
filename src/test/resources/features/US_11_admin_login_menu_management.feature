Feature: Admin login to access kitchen dashboard and manage dishes

  As a head chef,
  I want to access my login page by first selecting "Admin" and then "Køkken" from the main menu,
  So that I can securely reach the kitchen main menu.

  Scenario: Add new dish to menu
    Given I am on the Main application page
    And I have valid admin login credentials
    And the connection is secured via HTTPS
    And the login page is clearly labeled for kitchen staff or administrators
    When I enter my admin credentials in the login fields
    When I click the "Log ind" button on the login page
    Then I am redirected to the kitchen main menu
    And a confirmation or welcome message is displayed

  Scenario: Admin login fails with invalid credentials
    Given I am on the Main application page
    And I have invalid admin login credentials
    And the connection is secured via HTTPS
    And the login page is clearly labeled for kitchen staff or administrators
    When I enter my admin credentials in the login fields
    When I click the "Log ind" button on the login page
    Then the system highlights the invalid fields
    And an error message is displayed
