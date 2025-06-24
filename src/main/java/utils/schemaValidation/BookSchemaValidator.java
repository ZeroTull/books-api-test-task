package utils.schemaValidation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;

/**
 * Utility wrapper around Rest‑Assured's {@code matchesJsonSchemaInClasspath}
 * so step‑definitions can validate a response body against the canonical
 * <code>schemas/book-schema.json</code> without repeating plumbing.
 */
public final class BookSchemaValidator {

    private static final String BOOK_SCHEMA_PATH = "schemas/book-schema.json";

    private BookSchemaValidator() { /* static helpers only */ }

    /**
     * Assert that the given JSON string conforms to the book schema shipped in
     * <code>src/test/resources/schemas/book-schema.json</code>.
     */
    public static void assertValid(String json) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .then()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(BOOK_SCHEMA_PATH));
    }
}