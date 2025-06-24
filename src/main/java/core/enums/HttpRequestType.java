package core.enums;

public enum HttpRequestType {
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    UNKNOWN_REQUEST_TYPE;

    public static HttpRequestType fromString(String type) {
        for (HttpRequestType t : HttpRequestType.values()) {
            if (t.toString().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return HttpRequestType.UNKNOWN_REQUEST_TYPE;
    }
}
