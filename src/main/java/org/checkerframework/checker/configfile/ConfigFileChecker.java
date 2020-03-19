package org.checkerframework.checker.configfile;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;

public class ConfigFileChecker extends ValueChecker {
    public ConfigFileChecker() {
        super();
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new ConfigFileVisitor(this);
    }
}
