package core.restModel;

import config.ConfigReader;
import core.enums.HttpRequestType;
import core.enums.HttpStatusCode;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.path.json.config.JsonPathConfig;
import lombok.SneakyThrows;
import utils.JsonUtils;
import utils.RestAssuredUtils;

import java.util.Arrays;

/**
 * Thin HTTP façade.  All test layers should go through this class.
 */
public class HttpClient {
    //todo - extend bookService

    private final RestAssuredConfig restAssuredConfig;

    public HttpClient() {
        int timeoutSec = ConfigReader.get().getTimeout();

        this.restAssuredConfig = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutSec * 1000)
                        .setParam("http.socket.timeout", timeoutSec * 1000))
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));

        RestAssured.filters(Arrays.asList(
                new RequestLoggingFilter(LogDetail.ALL),
                new ResponseLoggingFilter(LogDetail.ALL)));
    }

    // ───────────────── Default CRUD wrappers ────────────────── //

    /**
     * POST and expect 201 Created
     */
    public <T> T post(String path, Object body, Class<T> as) {
        return send(path, body, HttpRequestType.POST, as, HttpStatusCode.CREATED_201.getCode());
    }

    public RestResponse postRaw(String path, Object body) {
        return RestAssuredUtils.execute(path, body, HttpRequestType.POST, restAssuredConfig);
    }

    /**
     * GET and expect 200 OK
     */
    public <T> T get(String path, Class<T> as) {
        return send(path, null, HttpRequestType.GET, as, HttpStatusCode.OK_200.getCode());
    }

    public RestResponse get(String path) {
        return send(path, null, HttpRequestType.GET, HttpStatusCode.OK_200.getCode());
    }

    @SneakyThrows
    public <T> T get(String path, Class<T> as, HttpStatusCode expected) {
        return send(path, null, HttpRequestType.GET, as, expected.getCode());
    }

    /**
     * PUT and expect 200 OK
     */
    public <T> T put(String path, Object body, Class<T> as) {
        return send(path, body, HttpRequestType.PUT, as, HttpStatusCode.OK_200.getCode());
    }

    /**
     * DELETE and expect 204 No-Content
     */
    public void delete(String path) {
        send(path, null, HttpRequestType.DELETE, Void.class, HttpStatusCode.DELETED_204.getCode());
    }

    // ───────────────── Overloads with custom expected codes ───────────────── //

    public <T> T post(String path, Object body, Class<T> as, int expected) {
        return send(path, body, HttpRequestType.POST, as, expected);
    }

    public RestResponse post(String uri, Object body) {
        return RestAssuredUtils.execute(uri, body, HttpRequestType.POST, restAssuredConfig);
    }

    public <T> T get(String p, Class<T> as, int expected) {
        return send(p, null, HttpRequestType.GET, as, expected);
    }

    public <T> T put(String p, Object b, Class<T> as, int e) {
        return send(p, b, HttpRequestType.PUT, as, e);
    }

    public void delete(String p, int expected) {
        send(p, null, HttpRequestType.DELETE, Void.class, expected);
    }

    // ───────────────── Raw helpers (no deserialization, no status assertion) ─ //

    public RestResponse getRaw(String url) {
        return RestAssuredUtils.execute(url, null, HttpRequestType.GET, restAssuredConfig);
    }

    public RestResponse deleteRaw(String url) {
        return RestAssuredUtils.execute(url, null, HttpRequestType.DELETE, restAssuredConfig);
    }

    // ───────────────── Core dispatchers ───────────────── //

    public RestResponse send(String uri, Object body, HttpRequestType requestType) {
        return RestAssuredUtils.execute(uri, body, requestType, restAssuredConfig);
    }

    public RestResponse send(String uri, Object body, HttpRequestType requestType, String username, String password) {
        return RestAssuredUtils.execute(uri, body, requestType, restAssuredConfig, username, password);
    }

    public RestResponse send(String uri, Object body, HttpRequestType requestType, int expected) {
        RestResponse res = send(uri, body, requestType);
        if (expected > 0 && res.getStatus() != expected) {
            throw new AssertionError("Expected HTTP " + expected + " but got " +
                    res.getStatus() + " — body: " + res.getBody());
        }
        return res;
    }

    private <T> T send(String uri, Object body, HttpRequestType requestType, Class<T> returnType, int expected) {
        RestResponse res = send(uri, body, requestType, expected);
        if (returnType == Void.class) return null;
        return JsonUtils.readValue(res.getBody(), returnType);
    }
}