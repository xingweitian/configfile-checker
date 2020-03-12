package org.checkerframework.checker.configfile;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

public class ConfigFileAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    public ConfigFileAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }
}
