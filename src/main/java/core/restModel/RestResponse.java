package core.restModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.Getter;
import utils.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP‑response DTO.  Keeps the public four‑argument constructor so the
 * rest of the test harness stays binary‑compatible, but also lets you build it
 * straight from a raw Rest‑Assured {@link Response} to capture additional
 * metadata (content‑type, cookies, method, URI, etc.).  <br>
 * Designed for test code – *not* a general‑purpose HTTP client.
 */
@Getter
public class RestResponse {
    // --- core fields  --------------------------------
    private final int status;
    private final String body;
    private final List<Header> headers;

    // --- extra field ---------------------------------
    private final Map<String, String> cookies;
    private final String contentType;
    private final String method;
    private final String uri;
    private final long latencyMs;


    public RestResponse(String contentType,
                        int status,
                        String body,
                        long latencyMs,
                        List<Header> headers) {
        this(status, body, latencyMs, headers, Collections.emptyMap(), contentType, null, null);
    }

    private RestResponse(int status,
                         String body,
                         long latencyMs,
                         List<Header> headers,
                         Map<String, String> cookies,
                         String contentType,
                         String method,
                         String uri) {
        this.status = status;
        this.body = body;
        this.latencyMs = latencyMs;
        this.headers = headers;
        this.cookies = cookies;
        this.contentType = contentType;
        this.method = method;
        this.uri = uri;
    }

    /**
     * Quick success guard (2xx range).
     */
    public void assertSuccess() {
        if (status < 200 || status > 299) {
            throw new AssertionError("Unexpected HTTP " + status + " – body: " + body);
        }
    }

    @JsonIgnore
    public <T> T getContent(Class<T> clazz) {
        return JsonUtils.read(getBody(), clazz);
    }


    @Override
    public String toString() {
        return "RestResponse{" +
                "status=" + status +
                ", latencyMs=" + latencyMs +
                ", contentType='" + contentType + '\'' +
                ", headers=" + headers.stream().map(Header::toString).collect(Collectors.joining(", ")) +
                ", cookies=" + cookies +
                ", body='" + (body != null ? body.substring(0, Math.min(120, body.length())) + "…" : "null") + '\'' +
                '}';
    }
}
