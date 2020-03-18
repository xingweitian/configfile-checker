package org.checkerframework.checker.configfile.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * This qualifier indicates the value of a key in the property file. E.g.,
 * {@code @ConfigFilePropertyValue("http://www.example.com") String url = prop.getProperty("URL");}
 */
@SubtypeOf({ConfigFileUnknown.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface ConfigFilePropertyValue {
    String value();
}
