import org.checkerframework.checker.configfile.qual.*;

// Test basic subtyping relationships for the Config Checker Checker.
class SubtypeTest {
    void allSubtypingRelationships(@ConfigFileUnknown int x, @ConfigFileBottom int y) {
        @ConfigFileUnknown int a = x;
        @ConfigFileUnknown int b = y;
        // :: error: assignment.type.incompatible
        @ConfigFileBottom int c = x; // expected error on this line
        @ConfigFileBottom int d = y;
    }
}
