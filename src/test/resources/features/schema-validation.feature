Feature: Contract – Books API list schema
  Ensures the /books endpoint still conforms to the agreed JSON schema.

  Scenario: Validate list schema
    When User sends a "GET" request to "/books"
    Then the response code should be 200
    And  the JSON response matches the book schema