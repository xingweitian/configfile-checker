package org.checkerframework.checker.config;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

public class ConfigAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    public ConfigAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }
}
