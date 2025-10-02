package ua.tymo.common.env;

/**
 * Enum representing available test environments.
 * The current environment is determined by the 'env' system property.
 */
public enum Environment {
    PROD,
    QA,
    STAGE,
    DEV;

    /**
     * Returns the current environment based on 'env' system property.
     * Defaults to PROD if not specified or invalid.
     */
    public static Environment current() {
        String env = System.getProperty("env", "prod").toUpperCase();
        try {
            return Environment.valueOf(env);
        } catch (IllegalArgumentException e) {
            return PROD;
        }
    }
}
