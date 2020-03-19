package org.checkerframework.checker.configfile;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueVisitor;

public class ConfigFileVisitor extends ValueVisitor {
    public ConfigFileVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected ValueAnnotatedTypeFactory createTypeFactory() {
        return new ConfigFileAnnotatedTypeFactory(this.checker);
    }
}
