package ua.tymo.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;


public final class ResponseWrapper<T> {
    private static final Logger log = LoggerFactory.getLogger(ResponseWrapper.class);
    private static final ObjectMapper M = JacksonProvider.mapper();

    private final Response raw;
    private final Class<T> type;

    public ResponseWrapper(Response raw, Class<T> type) {
        if (raw == null) {
            throw new IllegalStateException("Response is null");
        }
        this.raw = raw;
        this.type = type;
    }

    public Response raw() {
        return raw;
    }

    /** Asserts status code and returns this for chaining. */
    public ResponseWrapper<T> expectStatus(int expected) {
        int actual = raw.statusCode();
        if (actual != expected) {
            String body = safeBody();
            Assert.assertEquals(actual, expected,
                    "Unexpected HTTP status. Body: " + body);
        }
        return this;
    }

    /** Deserialize successful body to given type. */
    public T asBody() {
        String body = raw.asString();
        if (body == null || body.isEmpty()) {
            Assert.fail("Response body is empty; cannot map to " + type.getSimpleName());
        }
        try {
            log.debug("Deserializing to {}", type.getSimpleName());
            return M.readValue(body, type);
        } catch (Exception e) {
            Assert.fail("Failed to deserialize response to " + type.getSimpleName() +
                    ". Raw: " + body, e);
            return null;
        }
    }

    /** Deserialize error body to provided error class. */
    public <E> E asError(Class<E> errorType) {
        String body = raw.asString();
        if (body == null || body.isEmpty()) {
            Assert.fail("Response body is empty; cannot map to " + errorType.getSimpleName());
        }
        try {
            log.debug("Deserializing error to {}", errorType.getSimpleName());
            return M.readValue(body, errorType);
        } catch (Exception e) {
            Assert.fail("Failed to deserialize error to " + errorType.getSimpleName() +
                    ". Raw: " + body, e);
            return null; // unreachable
        }
    }

    private String safeBody() {
        try {
            String s = raw.asString();
            if (s == null) return "<empty>";
            if (s.length() > 2000) {
                return s.substring(0, 2000) + "...(truncated)";
            }
            return s;
        } catch (Exception e) {
            try {
                return "<<<binary " + M.writeValueAsString(raw.getBody()) + ">>>";
            } catch (JsonProcessingException ex) {
                return "<unreadable>";
            }
        }
    }
}
