Feature: DELETE /books – remove books
  Happy-path deletion plus idempotency, negative and edge-case coverage.

  Background:
    Given A book titled "Domain-Driven Design" by author "Eric Evans"
    And   I create the book

  # ───────────────────────── Happy path ─────────────────────────
  @happy @smoke
  @bug(api-returns-200)
  Scenario: Delete a book and confirm it is gone
    When  I delete created book
    Then  I should not be able to fetch that book anymore

  # ───────────────────── Negative+edge cases ─────────────────────
  @neg
  Scenario: Get all books with invalid credentials
    When  User sends a "DELETE" request to "/books/11212" using invalid credentials
    Then  the response code should be 401

  @neg
  @bug(api-returns-500)
  Scenario: Deleting the same book twice should return 404
    When  I delete created book
    And   I delete created book again
    Then  the response code should be 404

  @neg
  @bug(api-returns-500)
  Scenario Outline: Delete with a non-existent numeric ID
    When  User deletes book with id "<id>"
    Then  the response code should be 404
    Examples:
      | id                  |
      | 0                   |
      | -1                  |
      | 9223372036854775807 |
      | 9999999999          |

  @neg
  Scenario Outline: Delete with an invalid ID format
    When  User deletes book with id "<bad>"
    Then  the response code should be <code>
    Examples:
      | bad | code |
      | abc | 400  |
      |     | 405  |

  @neg
  Scenario Outline: Delete with an invalid ID format
    When  User deletes book with id "<bad>"
    Then  the response code should be <code>
    Examples:
      | bad | code |
      | abc | 400  |
      |     | 405  |