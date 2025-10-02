package ua.tymo.common.env;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:prod/config.properties"})
public interface APIConfig extends Config {

    @Key("api.timeout")
    @DefaultValue("5000")
    int apiTimeout();

    @Key("api.retries")
    @DefaultValue("2")
    int retries();
}
