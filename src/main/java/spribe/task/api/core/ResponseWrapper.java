package spribe.task.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Wrapper around RestAssured Response with type-safe deserialization and Allure integration.
 * Provides fluent API for response validation and logging.
 */
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
        logResponseDetails();
    }

    public Response raw() {
        return raw;
    }

    /**
     * Asserts status code and returns this for chaining.
     * Logs to Allure report.
     */
    public ResponseWrapper<T> expectStatus(int expected) {
        int actual = raw.statusCode();
        log.info("Expected status: {}, actual: {}", expected, actual);
        
        if (actual != expected) {
            String body = safeBody();
            String errorMsg = String.format("Unexpected HTTP status. Expected: %d, Actual: %d. Body: %s",
                    expected, actual, body);
            
            // Attach to Allure
            Allure.addAttachment("Expected Status", String.valueOf(expected));
            Allure.addAttachment("Actual Status", String.valueOf(actual));
            Allure.addAttachment("Response Body", body);
            
            log.error("Status code assertion failed: {}", errorMsg);
            Assert.assertEquals(actual, expected, errorMsg);
        }
        return this;
    }

    /**
     * Deserialize successful body to given type.
     * Logs and attaches to Allure.
     */
    public T asBody() {
        String body = raw.asString();
        if (body == null || body.isEmpty()) {
            log.error("Response body is empty");
            Allure.addAttachment("Error", "Response body is empty");
            Assert.fail("Response body is empty; cannot map to " + type.getSimpleName());
        }
        try {
            log.info("Deserializing response body to {}", type.getSimpleName());
            T result = M.readValue(body, type);
            
            try {
                String prettyJson = M.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                Allure.addAttachment("Response Body (" + type.getSimpleName() + ")", "application/json", prettyJson);
            } catch (Exception e) {
                Allure.addAttachment("Response Body (raw)", body);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to deserialize response to {}: {}", type.getSimpleName(), e.getMessage());
            Allure.addAttachment("Deserialization Error", e.getMessage());
            Allure.addAttachment("Raw Response", body);
            Assert.fail("Failed to deserialize response to " + type.getSimpleName() +
                    ". Raw: " + body, e);
            return null;
        }
    }

    /**
     * Deserialize error body to provided error class.
     * Logs and attaches to Allure.
     */
    public <E> E asError(Class<E> errorType) {
        String body = raw.asString();
        if (body == null || body.isEmpty()) {
            log.warn("Error response body is empty");
            Allure.addAttachment("Warning", "Error response body is empty");
            Assert.fail("Response body is empty; cannot map to " + errorType.getSimpleName());
        }
        try {
            log.info("Deserializing error response to {}", errorType.getSimpleName());
            E error = M.readValue(body, errorType);
            
            // Attach error details to Allure
            try {
                String prettyJson = M.writerWithDefaultPrettyPrinter().writeValueAsString(error);
                Allure.addAttachment("Error Response (" + errorType.getSimpleName() + ")", "application/json", prettyJson);
            } catch (Exception e) {
                Allure.addAttachment("Error Response (raw)", body);
            }
            
            return error;
        } catch (Exception e) {
            log.error("Failed to deserialize error to {}: {}", errorType.getSimpleName(), e.getMessage());
            Allure.addAttachment("Deserialization Error", e.getMessage());
            Allure.addAttachment("Raw Error Response", body);
            Assert.fail("Failed to deserialize error to " + errorType.getSimpleName() +
                    ". Raw: " + body, e);
            return null;
        }
    }

    /**
     * Logs basic response details (status, content type, response time).
     */
    private void logResponseDetails() {
        int statusCode = raw.statusCode();
        String contentType = raw.getContentType();
        long responseTime = raw.getTime();
        
        log.info("Response received: status={}, contentType={}, time={}ms", 
                statusCode, contentType, responseTime);
        
        // Add response metrics to Allure
        Allure.addAttachment("Status Code", String.valueOf(statusCode));
        Allure.addAttachment("Content Type", contentType != null ? contentType : "N/A");
        Allure.addAttachment("Response Time", responseTime + " ms");
    }

    /**
     * Safely extracts response body for error messages.
     */
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
