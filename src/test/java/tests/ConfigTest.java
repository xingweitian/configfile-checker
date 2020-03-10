import java.io.File;
import java.util.List;
import org.checkerframework.checker.config.ConfigChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test runner for tests of the Config Checker Checker.
 *
 * <p>Tests appear as Java files in the {@code tests/config} folder. To add a new test case,
 * create a Java file in that directory. The file contains "// ::" comments to indicate expected
 * errors and warnings; see
 * https://github.com/typetools/checker-framework/blob/master/checker/tests/README .
 */
public class ConfigTest extends CheckerFrameworkPerDirectoryTest {
    public ConfigTest(List<File> testFiles) {
        super(
                testFiles,
                ConfigChecker.class,
                "config",
                "-Anomsgtext",
                "-Astubs=stubs/",
                "-nowarn");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"config"};
    }
}
