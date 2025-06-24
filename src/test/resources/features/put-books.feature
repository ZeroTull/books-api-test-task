Feature: PUT /books – update books

  Background:
    Given A book titled "Refactoring" by author "Martin Fowler"
    And   I create the book

  # ───────────────────────── Happy path ─────────────────────────
  @smoke
  Scenario: Update the title of an existing book
    When  I update the title to "For the Emperor"
    Then  the title should be "For the Emperor"

  @happy @smoke
  Scenario: Update every field except id
    When I update the book with:
      | name        | Emperors Light  |
      | author      | For the Emperor |
      | category    | Holy category   |
      | publication | W40K            |
      | pages       | 777             |
      | price       | 1000.11         |

    Then the response code should be 200
    And  the book should be updated correctly

  # ───────────────────── Negative+edge cases ─────────────────────
  @neg
  Scenario: Update a book with invalid ID
    When  User sends a "PUT" request to "/books/abc"
    Then  the response code should be 400

  @neg
  Scenario Outline: Unauthorised access to write endpoints
    When  User sends a "PUT" request to "/books<suffix>" using invalid credentials
    Then  the response code should be 401
    Examples:
      | suffix  |
      | /       |
      | /999999 |

  @neg
  Scenario: Update non-existing ID → 404
    When  User sends a "PUT" request to "/books/999999"
    Then  the response code should be 404