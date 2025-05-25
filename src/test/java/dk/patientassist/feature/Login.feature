Feature: Kitchen staff can log in through the system

  As a kitchen staff member
  I want to log in through the system's backend
  So that I can access the meal overview

  Scenario: Logging in with correct username and password
    Given there is a user with the username "kitchenstaff" and password "test123"
    When the user tries to log in with these details
    Then the system should respond with status 200 (OK)
    And the response should include a valid login token
    And the response should include the username "kitchenstaff"

  Scenario: Logging in with the wrong password
    Given there is a user with the username "kitchenstaff" and password "test123"
    When the user tries to log in using the password "wrongpass"
    Then the system should respond with status 401 (Unauthorized)
    And the response should contain the message "Wrong password"

  Scenario: Logging in with a username that doesn't exist
    When someone tries to log in with the username "ghost" and any password
    Then the system should respond with status 401 (Unauthorized)
    And the response should contain the message "No user found with username: ghost"

  Scenario: Logging in without providing any details
    When someone tries to log in without entering a username or password
    Then the system should respond with status 400 (Bad Request)
    And the response should include an error message indicating invalid input