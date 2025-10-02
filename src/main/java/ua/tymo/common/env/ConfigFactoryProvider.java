package ua.tymo.common.env;

import org.aeonbits.owner.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides singleton access to configuration instances.
 */
public final class ConfigFactoryProvider {

    private static final TestConfig TEST_CONFIG;

    static {
        Map<String, String> configMap = new HashMap<>();
        String env = System.getProperty("env", "prod").toLowerCase();
        configMap.put("env", env);
        TEST_CONFIG = ConfigFactory.create(TestConfig.class, configMap);
    }

    private ConfigFactoryProvider() {}

    /**
     * Returns unified test configuration instance.
     */
    public static TestConfig config() {
        return TEST_CONFIG;
    }

    /**
     * @deprecated Use config() instead. Kept for backward compatibility.
     */
    @Deprecated
    public static AppConfig app() {
        return (AppConfig) TEST_CONFIG;
    }

    /**
     * @deprecated Use config() instead. Kept for backward compatibility.
     */
    @Deprecated
    public static APIConfig api() {
        return (APIConfig) TEST_CONFIG;
    }
}
