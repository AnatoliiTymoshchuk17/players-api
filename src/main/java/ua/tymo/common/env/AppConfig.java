package ua.tymo.common.env;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:prod/config.properties"})
public interface AppConfig extends Config {

    @Key("app.baseUrl")
    @DefaultValue("http://3.68.165.45")
    String baseUrl();

    @Key("editor.supervisor")
    @DefaultValue("supervisor")
    String supervisor();

    @Key("editor.admin")
    @DefaultValue("admin")
    String admin();

    @Key("test.thread.count")
    @DefaultValue("3")
    int threadCount();
}
