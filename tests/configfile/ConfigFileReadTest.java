import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.checkerframework.checker.configfile.qual.ConfigFilePropertyValue;

class ConfigFileReadTest {

    public static final String propFile = "tests/configfile/a.properties";

    void a() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        @ConfigFilePropertyValue("http://www.example.com")
        String url = prop.getProperty("URL");
    }

    void b() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        @ConfigFilePropertyValue("localhost")
        String host = prop.getProperty("HOST", "127.0.0.1");
    }

    void c() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFile);
        prop.load(inputStream);
        @ConfigFilePropertyValue("default value")
        String host = prop.getProperty("NOSUCHKEY", "default value");
    }
}
