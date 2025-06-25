Feature: POST /books – create new books

  # ───────────────────────── Happy path ─────────────────────────
  @bug(api-returns-200)
  @smoke
  Scenario: POST must return **201 Created**
    When  User sends a "POST" request to "/books"
    # will currently fail (200)
    Then  the response code should be 201

  @happy @smoke
  Scenario: Successfully create a new book
    Given A book titled "The Pragmatic Cadia" by author "Imperial guard"
    When  I create the book
    Then  the created book should have an ID assigned
    And   the title should be "The Pragmatic Cadia"
    And   the author should be "Imperial guard"

  # ───────────────────── Negative+edge cases ─────────────────────
  @neg
  @bug(api-returns-200)
  Scenario: Creating a duplicate book should return conflict
    Given A book titled "Ordo Xenos" by author "Velayne Ramaeus"
    And   I create the book
    When  I create the book again
    Then  the response code should be 409

  @neg
  Scenario Outline: Unauthorised access to write endpoints
    When  User sends a "POST" request to "/books<suffix>" using invalid credentials
    Then  the response code should be 401
    Examples:
      | suffix  |
      | /       |
      | /999999 |

  @neg
  Scenario: Creating a book with an empty body returns 400
    When  I send a request with "" body to "/books"
    Then  the response code should be 400

  @neg
  @bug(api-returns-500)
  Scenario: Creating a book with an empty body returns 400
    When  I send a request with "null" body to "/books"
    Then  the response code should be 400

  @neg
  Scenario: Create a book with invalid pages type
    Given I have a book payload with "qqq" in "pages" field
    When  User sends a "POST" request to "/books"
    Then  the response code should be 400

  @neg
  Scenario: Create a book with invalid price type
    Given I have a book payload with "test" in "price" field
    When  User sends a "POST" request to "/books"
    Then  the response code should be 400

  @neg
  @bug(api-returns-500)
  Scenario: Create a book with large payload
    Given I have a book payload with 1MB of text
    When  User sends a "POST" request to "/books"
    Then  the response code should be 413

  @neg
  Scenario: Create a book with every field empty
    Given I have a book payload with all fields blank
    When  User sends a "POST" request to "/books"
    Then  the response code should be 400