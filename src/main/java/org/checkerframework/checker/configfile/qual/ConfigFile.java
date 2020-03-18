package org.checkerframework.checker.configfile.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * If an object has type {@code @ConfigFile("a.property")}, then this object has type {@link
 * java.io.InputStream} or {@link java.util.Properties} which loads the property file: a.property.
 */
@SubtypeOf({ConfigFileUnknown.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface ConfigFile {
    String value();
}
