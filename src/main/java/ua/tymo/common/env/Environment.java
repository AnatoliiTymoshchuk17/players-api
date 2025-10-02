package ua.tymo.common.env;

public enum Environment {
    PROD,
    DEV;

    public static Environment current() {
        String env = System.getProperty("env", "prod").toUpperCase();
        try {
            return Environment.valueOf(env);
        } catch (IllegalArgumentException e) {
            return PROD;
        }
    }
}

