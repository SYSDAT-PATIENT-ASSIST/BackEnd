Feature: Login and Role Management

  As a user of the system,
  I want to log in and access endpoints based on my role,
  So that the system ensures correct authorization.

  Scenario: Successful login with valid credentials
    Given I am a registered user with username "chef" and password "password123"
    When I send a POST request to "/auth/login" with my credentials
    Then I receive a 200 OK response
    And I receive a valid JWT token

  Scenario: Failed login with invalid credentials
    Given I am not registered
    When I send a POST request to "/auth/login" with username "invalid" and password "wrong"
    Then I receive a 401 Unauthorized response

  Scenario: Access protected endpoint as Head Chef
    Given I am logged in as "chef" with role "HEAD_CHEF"
    When I send a POST request to "/dishes" with valid dish data
    Then I receive a 201 Created response

  Scenario: Access denied for unauthorized user
    Given I am logged in as "patient" with role "USER"
    When I send a POST request to "/dishes" with valid dish data
    Then I receive a 403 Forbidden response
