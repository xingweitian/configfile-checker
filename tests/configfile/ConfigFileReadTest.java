import org.checkerframework.checker.configfile.qual.ConfigFile;
import org.checkerframework.checker.configfile.qual.ConfigFilePropertyValue;
import org.checkerframework.common.value.qual.StringVal;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class ConfigFileReadTest {
    void a() throws IOException{
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tests/configfile/a.properties");
        prop.load(inputStream);
        @ConfigFilePropertyValue("http://www.example.com") String url = prop.getProperty("URL");
    }
}
