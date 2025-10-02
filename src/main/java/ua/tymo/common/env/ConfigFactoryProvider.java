package ua.tymo.common.env;

import org.aeonbits.owner.ConfigFactory;

public final class ConfigFactoryProvider {

    private static final AppConfig appConfig = ConfigFactory.create(AppConfig.class, System.getProperties());
    private static final APIConfig apiConfig = ConfigFactory.create(APIConfig.class, System.getProperties());

    private ConfigFactoryProvider() {}

    public static AppConfig app() {
        return appConfig;
    }

    public static APIConfig api() {
        return apiConfig;
    }
}
