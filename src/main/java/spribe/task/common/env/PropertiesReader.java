package spribe.task.common.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesReader {

    private PropertiesReader() {}

    public static Properties read(String resourcePath) {
        Properties props = new Properties();
        try (InputStream in = PropertiesReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
        }
        return props;
    }
}
