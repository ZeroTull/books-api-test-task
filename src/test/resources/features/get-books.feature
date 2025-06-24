Feature: GET /books – retrieve books
  Covers single-item fetch plus list, pagination, filters, sort and negative paths.

  Background:
    Given A book titled "Clean Code" by author "Robert C. Martin"
    And   I create the book

  # ───────────────────────── Happy paths ─────────────────────────
  @happy @smoke
  Scenario: Fetch the created book by ID
    When  I retrieve that book by ID
    Then  the title should be "Clean Code"
    And   the author should be "Robert C. Martin"

  @happy @smoke
  Scenario: Get all books
    When  User sends request to get all available books
    Then  the response code should be 200
    And   the response should contain a list of books

  # ───────────────────── Negative+edge cases ─────────────────────
  @neg
  Scenario: Get all books with invalid credentials
    When  User sends a "GET" request to "/books" using invalid credentials
    Then  the response code should be 401

  @neg
  Scenario: Get book by valid ID
    When  User sends a "GET" request to "/books/11177"
    Then  the response code should be 200
    And   the book should have id 11177

  @neg
  Scenario: Get book by non-existing ID
    When  User sends a "GET" request to "/books/999999"
    Then  the response code should be 404

  @neg
  Scenario: Get book with invalid ID format
    When  User sends a "GET" request to "/books/abc123"
    Then  the response code should be 400

  @neg
  Scenario Outline: Get book with a non-existent numeric ID
    When  User sends a "GET" request to "/books/<id>"
    Then  the response code should be <code>
    Examples:
      | id         | code |
      | 0          | 404  |
      | -1         | 404  |
      | 2147483647 | 404  |
      | 9999999999 | 400  |