package ua.tymo.common.env;

import org.aeonbits.owner.Config;

/**
 * Unified configuration interface for all test framework settings.
 * Reads from config.properties files based on environment.
 * System properties override file properties.
 * Extends AppConfig and APIConfig for backward compatibility.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:${env}/config.properties",
        "classpath:prod/config.properties"
})
public interface TestConfig extends AppConfig, APIConfig {

    // Editor/User settings (in addition to inherited from AppConfig)
    @Key("editor.supervisor")
    @DefaultValue("supervisor")
    String supervisorLogin();

    @Key("editor.admin")
    @DefaultValue("admin")
    String adminLogin();

    // API endpoints
    @Key("endpoint.player.create")
    @DefaultValue("/player/create/{editor}")
    String endpointPlayerCreate();

    @Key("endpoint.player.get")
    @DefaultValue("/player/get")
    String endpointPlayerGet();

    @Key("endpoint.player.getAll")
    @DefaultValue("/player/get/all")
    String endpointPlayerGetAll();

    @Key("endpoint.player.update")
    @DefaultValue("/player/update/{editor}/{id}")
    String endpointPlayerUpdate();

    @Key("endpoint.player.delete")
    @DefaultValue("/player/delete/{editor}")
    String endpointPlayerDelete();

    // Test execution settings
    @Key("test.retry.count")
    @DefaultValue("1")
    int retryCount();

    // Allure settings
    @Key("allure.results.directory")
    @DefaultValue("target/allure-results")
    String allureResultsDirectory();

    // Test data generation settings
    @Key("test.user.min.age")
    @DefaultValue("16")
    int minAge();

    @Key("test.user.max.age")
    @DefaultValue("60")
    int maxAge();

    @Key("test.password.min.length")
    @DefaultValue("7")
    int minPasswordLength();

    @Key("test.password.max.length")
    @DefaultValue("15")
    int maxPasswordLength();
}

