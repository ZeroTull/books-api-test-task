package utils.restAssured;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Logs request/response like Rest-Assured’s default filters
 * but suppresses large bodies (&gt; THRESHOLD bytes).
 */
public class SmartLoggingFilter implements Filter {

    /** 200 KB – tweak as you wish */
    private static final int THRESHOLD = 200 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(SmartLoggingFilter.class);

    @Override
    public io.restassured.response.Response filter(FilterableRequestSpecification req,
                                                   FilterableResponseSpecification res,
                                                   FilterContext ctx) {

        /* ────────────────  REQUEST  ──────────────── */
        byte[] reqBytes = req.getBody() == null
                ? new byte[0]
                : req.getBody().toString().getBytes(StandardCharsets.UTF_8);

        if (reqBytes.length <= THRESHOLD) {
            LOG.info("[REQ] {} {}{}\n{}",
                    req.getMethod(), req.getURI(),
                    reqBytes.length > 0 ? "" : " (no body)",
                    reqBytes.length > 0 ? req.getBody() : "");
        } else {
            LOG.info("[REQ] {} {} (body suppressed, {} bytes)",
                    req.getMethod(), req.getURI(), reqBytes.length);
        }

        /* ─── forward the call and capture response ─── */
        io.restassured.response.Response response = ctx.next(req, res);

        /* ────────────────  RESPONSE  ──────────────── */
        byte[] respBytes = response.getBody() == null
                ? new byte[0]
                : response.getBody().asByteArray();

        if (respBytes.length <= THRESHOLD) {
            LOG.info("[RES] {} {} {}\n{}",
                    response.getStatusCode(),
                    response.getStatusLine(),
                    response.getContentType(),
                    response.getBody().asString());
        } else {
            LOG.info("[RES] {} {} {} (body suppressed, {} bytes)",
                    response.getStatusCode(),
                    response.getStatusLine(),
                    response.getContentType(),
                    respBytes.length);
        }

        return response;
    }
}