package utils;

import config.ConfigReader;
import core.enums.HttpRequestType;
import core.restModel.RestResponse;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * One-stop utility for building a {@link RequestSpecification} with common defaults
 * and executing an HTTP call while capturing the essentials (status, body, headers).
 * <p>
 * Tests should not call Rest-Assured directly; go through {@code HttpClient},
 * which in turn delegates to these helpers.
 */
public final class RestAssuredUtils {

    private RestAssuredUtils() { /* static only */ }

    // ------------------------------------------------------------------ //
    //  Spec builder – shared across every call
    // ------------------------------------------------------------------ //

    private static RequestSpecification requestSpec(String username, String password) {
        RequestSpecification spec = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (username != null && password != null) {
            spec.auth().preemptive().basic(username, password);
        }
        return spec;
    }

    // ------------------------------------------------------------------ //
    //  Dispatcher
    // ------------------------------------------------------------------ //

    public static RestResponse execute(String uri, Object payload, HttpRequestType type, RestAssuredConfig cfg) {
        String user = ConfigReader.get().getUsername();
        String pass = ConfigReader.get().getPassword();
        return execute(uri, payload, type, cfg, user, pass);
    }

    public static RestResponse execute(String uri, Object payload, HttpRequestType type, RestAssuredConfig cfg, String userName, String password) {
        Response raResponse;
        RequestSpecification specification = requestSpec(userName, password).config(cfg);
        switch (type) {
            case GET:
                raResponse = specification.when().get(uri);
                break;
            case POST:
                raResponse = specification.body(payload).when().post(uri);
                break;
            case PUT:
                raResponse = specification.body(payload).when().put(uri);
                break;
            case DELETE:
                raResponse = specification.when().delete(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported requestType: " + type);
        }

        return new RestResponse(
                raResponse.getContentType(),
                raResponse.statusCode(),
                raResponse.getBody().asString(),
                raResponse.getTime(),
                raResponse.headers().asList());
    }
}