Feature: Admin kitchen login flow

  As a head chef
  I want to access my login page by first selecting "Admin" and then "Køkken" from the main menu
  So that I can securely reach the kitchen main menu

  Background:
    Given I am on the Main application page
    And the connection is secured via HTTPS
    And the login page is clearly labeled for kitchen staff or administrators

  Scenario: Successful login and access to kitchen menu
    Given I have valid admin login credentials
    When I enter my admin credentials in the login fields
    And I click the "Log ind" button on the login page
    Then I am redirected to the kitchen main menu
    And a confirmation or welcome message is displayed

  Scenario: Failed login due to invalid credentials
    Given I have invalid admin login credentials
    When I enter my admin credentials in the login fields
    And I click the "Log ind" button on the login page
    Then the system highlights the invalid fields
    And an error message is displayed
