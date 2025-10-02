package ua.tymo.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import ua.tymo.common.env.ConfigFactoryProvider;

/**
 * TestNG retry analyzer that retries failed tests based on configuration.
 * Retry count is configured via TestConfig.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int maxAttempts;

    public RetryAnalyzer() {
        this.maxAttempts = ConfigFactoryProvider.config().retryCount();
    }

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxAttempts) {
            attempt++;
            return true;
        }
        return false;
    }
}
