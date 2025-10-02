package ua.tymo.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;


public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int maxAttempts;

    public RetryAnalyzer() {
        this.maxAttempts = Integer.parseInt(System.getProperty("test.retry.count",
                System.getenv().getOrDefault("TEST_RETRY_COUNT", "1")));
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
