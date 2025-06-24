package core.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum HttpStatusCode {
    OK_200(200),
    CREATED_201(201),
    DELETED_204(204),
    UNAUTHORIZED_401(401),
    BAD_REQUEST_400(400),
    NOT_FOUND_404(404),
    UNSUPPORTED_MEDIA_TYPE_415(415),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatusCode(int code) {
        this.code = code;
    }

    public static HttpStatusCode fromCode(String code) {
        return Arrays.stream(HttpStatusCode.values())
                .filter(httpStatusCode -> code.equals(String.valueOf(httpStatusCode.getCode())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknow status:" + code));
    }
}
