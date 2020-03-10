import org.checkerframework.checker.config.qual.*;

// Test basic subtyping relationships for the Config Checker Checker.
class SubtypeTest {
    void allSubtypingRelationships(@ConfigUnknown int x, @ConfigBottom int y) {
        @ConfigUnknown int a = x;
        @ConfigUnknown int b = y;
        // :: error: assignment.type.incompatible
        @ConfigBottom int c = x; // expected error on this line
        @ConfigBottom int d = y;
    }
}
