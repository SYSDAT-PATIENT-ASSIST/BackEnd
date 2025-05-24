Feature: Security endpoints are protected by roles

  Background:
    Given the API is up on "http://localhost:7070"

  Scenario Outline: Register, login, and access a protected endpoint
    Given I register with username "<user>" and password "<pass>" as "<role>"
    When I log in with username "<user>" and password "<pass>"
    Then the login status code is 200
    And I save the returned token
    When I access "<endpoint>" with that token
    Then the status code should be <expectedCode>

    Examples:
      | user  | pass     | role      | endpoint                  | expectedCode |
      | alice | secret1  | ADMIN     | /protected/admin_demo     | 200          |
      | bob   | secret2  | HOVEDKOK  | /protected/admin_demo     | 403          |  # non-admin role
      | carol | secret3  | HOVEDKOK  | /protected/user_demo      | 403          |  # same
