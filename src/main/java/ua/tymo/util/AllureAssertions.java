package ua.tymo.util;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import ua.tymo.api.model.error.ErrorBody;
import ua.tymo.api.model.response.PlayerResponse;

/**
 * Assertion helper methods with Allure @Step annotations for better reporting.
 * Each assertion is logged as a separate step in Allure report.
 */
public final class AllureAssertions {

    private static final Logger log = LoggerFactory.getLogger(AllureAssertions.class);

    private AllureAssertions() {}

    @Step("Verify HTTP status code is {expected}")
    public static void assertStatusCode(int actual, int expected) {
        log.info("Asserting status code: expected={}, actual={}", expected, actual);
        Allure.addAttachment("Expected Status", String.valueOf(expected));
        Allure.addAttachment("Actual Status", String.valueOf(actual));
        Assert.assertEquals(actual, expected, "HTTP status code mismatch");
    }

    @Step("Verify player field: {fieldName} equals '{expected}'")
    public static void assertPlayerField(String fieldName, Object actual, Object expected) {
        log.info("Asserting player field '{}': expected={}, actual={}", fieldName, expected, actual);
        Assert.assertEquals(actual, expected, fieldName + " mismatch");
    }

    @Step("Verify player ID is {expected}")
    public static void assertPlayerId(Integer actual, Integer expected) {
        log.info("Asserting player ID: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Player ID mismatch");
    }

    @Step("Verify login is '{expected}'")
    public static void assertLogin(String actual, String expected) {
        log.info("Asserting login: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Login mismatch");
    }

    @Step("Verify role is '{expected}'")
    public static void assertRole(String actual, String expected) {
        log.info("Asserting role: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Role mismatch");
    }

    @Step("Verify age is {expected}")
    public static void assertAge(Integer actual, Integer expected) {
        log.info("Asserting age: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Age mismatch");
    }

    @Step("Verify gender is '{expected}'")
    public static void assertGender(String actual, String expected) {
        log.info("Asserting gender: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Gender mismatch");
    }

    @Step("Verify screen name is '{expected}'")
    public static void assertScreenName(String actual, String expected) {
        log.info("Asserting screen name: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Screen name mismatch");
    }

    @Step("Verify password is '{expected}'")
    public static void assertPassword(String actual, String expected) {
        log.info("Asserting password: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Password mismatch");
    }

    @Step("Verify error message contains '{expectedSubstring}'")
    public static void assertErrorContains(String errorMessage, String expectedSubstring) {
        log.info("Asserting error message contains: '{}'", expectedSubstring);
        String lowerCaseError = errorMessage.toLowerCase();
        String lowerCaseExpected = expectedSubstring.toLowerCase();
        Allure.addAttachment("Error Message", errorMessage);
        Allure.addAttachment("Expected Substring", expectedSubstring);
        Assert.assertTrue(lowerCaseError.contains(lowerCaseExpected),
                String.format("Error message '%s' does not contain '%s'", errorMessage, expectedSubstring));
    }

    @Step("Verify error title contains one of: {expectedSubstrings}")
    public static void assertErrorContainsAny(ErrorBody error, String... expectedSubstrings) {
        String errorTitle = error.getTitle().toLowerCase();
        log.info("Asserting error title '{}' contains one of: {}", error.getTitle(), String.join(", ", expectedSubstrings));
        Allure.addAttachment("Error Title", error.getTitle());
        Allure.addAttachment("Expected Substrings", String.join(", ", expectedSubstrings));
        
        for (String expected : expectedSubstrings) {
            if (errorTitle.contains(expected.toLowerCase())) {
                return;
            }
        }
        Assert.fail(String.format("Error title '%s' does not contain any of: %s",
                error.getTitle(), String.join(", ", expectedSubstrings)));
    }

    @Step("Verify player object matches expected values")
    public static void assertPlayerMatches(PlayerResponse actual, PlayerResponse expected) {
        log.info("Asserting player matches: ID={}", expected.getPlayerId());
        assertPlayerId(actual.getPlayerId(), expected.getPlayerId());
        assertLogin(actual.getLogin(), expected.getLogin());
        assertRole(actual.getRole(), expected.getRole());
        if (expected.getAge() != null) {
            assertAge(actual.getAge(), expected.getAge());
        }
        if (expected.getGender() != null) {
            assertGender(actual.getGender(), expected.getGender());
        }
        if (expected.getScreenName() != null) {
            assertScreenName(actual.getScreenName(), expected.getScreenName());
        }
    }

    @Step("Verify value is not null")
    public static void assertNotNull(Object actual, String message) {
        log.info("Asserting not null: {}", message);
        Assert.assertNotNull(actual, message);
    }

    @Step("Verify condition is true: {message}")
    public static void assertTrue(boolean condition, String message) {
        log.info("Asserting true: {}", message);
        Allure.addAttachment("Condition", message);
        Assert.assertTrue(condition, message);
    }

    @Step("Verify player count is {expected}")
    public static void assertPlayerCount(int actual, int expected) {
        log.info("Asserting player count: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected, "Player count mismatch");
    }
}

