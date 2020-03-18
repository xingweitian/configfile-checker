package org.checkerframework.checker.configfile;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Properties;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.configfile.qual.ConfigFile;
import org.checkerframework.checker.configfile.qual.ConfigFileBottom;
import org.checkerframework.checker.configfile.qual.ConfigFilePropertyValue;
import org.checkerframework.checker.configfile.qual.ConfigFileUnknown;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

public class ConfigFileAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    private final ExecutableElement getResourceAsStream =
            TreeUtils.getMethod(
                    java.lang.ClassLoader.class.getName(), "getResourceAsStream", 1, processingEnv);

    private final ExecutableElement getProperty =
            TreeUtils.getMethod(
                    java.util.Properties.class.getName(), "getProperty", 1, processingEnv);

    private final ExecutableElement getPropertyWithDefaultValue =
            TreeUtils.getMethod(
                    java.util.Properties.class.getName(), "getProperty", 2, processingEnv);

    public ConfigFileAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new ConfigFileQualifierHierarchy(
                factory, AnnotationBuilder.fromClass(elements, ConfigFileBottom.class));
    }

    private static class ConfigFileQualifierHierarchy extends GraphQualifierHierarchy {

        public ConfigFileQualifierHierarchy(MultiGraphFactory factory, AnnotationMirror bottom) {
            super(factory, bottom);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByClass(subAnno, ConfigFileBottom.class)
                    || AnnotationUtils.areSameByClass(superAnno, ConfigFileUnknown.class)) {
                return true;
            } else if (AnnotationUtils.areSameByClass(subAnno, ConfigFileUnknown.class)
                    || AnnotationUtils.areSameByClass(superAnno, ConfigFileBottom.class)) {
                return false;
            } else if (AnnotationUtils.areSameByClass(subAnno, ConfigFile.class)
                    && AnnotationUtils.areSameByClass(superAnno, ConfigFile.class)) {
                return compareElementValue(subAnno, superAnno);
            } else if (AnnotationUtils.areSameByClass(subAnno, ConfigFilePropertyValue.class)
                    && AnnotationUtils.areSameByClass(superAnno, ConfigFilePropertyValue.class)) {
                return compareElementValue(subAnno, superAnno);
            } else {
                return false;
            }
        }

        private boolean compareElementValue(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            String subAnnoElementValue =
                    AnnotationUtils.getElementValue(subAnno, "value", String.class, false);
            String superAnnoElementValue =
                    AnnotationUtils.getElementValue(superAnno, "value", String.class, false);
            return superAnnoElementValue.equals(subAnnoElementValue);
        }
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                super.createTreeAnnotator(), new ConfigFileTreeAnnotator(this));
    }

    private class ConfigFileTreeAnnotator extends TreeAnnotator {

        public ConfigFileTreeAnnotator(AnnotatedTypeFactory atypefactory) {
            super(atypefactory);
        }

        @Override
        public Void visitMethodInvocation(
                MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {

            if (TreeUtils.isMethodInvocation(node, getResourceAsStream, processingEnv)) {

                AnnotationMirror stringValAnnoMirror = getStringValAnnoMirrorOfArgument(node, 0);

                if (stringValAnnoMirror == null) {
                    return super.visitMethodInvocation(node, annotatedTypeMirror);
                }

                String propFile = getValueFromStringValAnnoMirror(stringValAnnoMirror);

                if (propFile != null) {
                    annotatedTypeMirror.replaceAnnotation(
                            createAnnotation(propFile, ConfigFile.class));
                }

            } else if (TreeUtils.isMethodInvocation(node, getProperty, processingEnv)) {

                AnnotationMirror configFileAnnoMirror =
                        atypeFactory.getReceiverType(node).getAnnotation(ConfigFile.class);
                AnnotationMirror stringValAnnoMirror = getStringValAnnoMirrorOfArgument(node, 0);

                if (configFileAnnoMirror != null && stringValAnnoMirror != null) {

                    String propFile = getValueFromConfigFileAnnoMirror(configFileAnnoMirror);

                    if (propFile != null) {

                        String argValue = getValueFromStringValAnnoMirror(stringValAnnoMirror);
                        String propertyValue = readPropertyFromFile(propFile, argValue);

                        if (propertyValue != null) {
                            annotatedTypeMirror.replaceAnnotation(
                                    createAnnotation(propertyValue, ConfigFilePropertyValue.class));
                        }
                    }
                }
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }

        private String readPropertyFromFile(String fileName, String argValue) {
            String res = null;
            try {
                Properties prop = new Properties();

                ClassLoader cl = this.getClass().getClassLoader();
                if (cl == null) {
                    // the class loader is null if the system class loader was
                    // used
                    cl = ClassLoader.getSystemClassLoader();
                }
                InputStream in = cl.getResourceAsStream(fileName);

                if (in == null) {
                    // if the classloader didn't manage to load the file, try
                    // whether a FileInputStream works. For absolute paths this
                    // might help.
                    try {
                        in = new FileInputStream(fileName);
                    } catch (FileNotFoundException e) {
                        // ignore
                    }
                }

                if (in == null) {
                    checker.message(Kind.WARNING, "Couldn't find the properties file: " + fileName);
                    return null;
                }

                prop.load(in);
                res = prop.getProperty(argValue);
            } catch (Exception e) {
                checker.message(
                        Kind.WARNING, "Exception in ConfigFileChecker.readPropertyFromFile: " + e);
                e.printStackTrace();
            }
            return res;
        }
    }

    private ValueAnnotatedTypeFactory getValueAnnotatedTypeFactory() {
        return getTypeFactoryOfSubchecker(ValueChecker.class);
    }

    protected AnnotationMirror createAnnotation(
            String value, Class<? extends Annotation> className) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, className);
        builder.setValue("value", value);
        return builder.build();
    }

    private String getValueFromStringValAnnoMirror(AnnotationMirror stringValAnnoMirror) {
        List<String> values =
                AnnotationUtils.getElementValueArray(
                        stringValAnnoMirror, "value", String.class, true);
        if (!values.isEmpty()) {
            return values.get(0);
        } else {
            return null;
        }
    }

    protected static String getValueFromConfigFileAnnoMirror(
            AnnotationMirror configFileAnnoMirror) {
        return AnnotationUtils.getElementValue(configFileAnnoMirror, "value", String.class, false);
    }

    private AnnotationMirror getStringValAnnoMirrorOfArgument(
            MethodInvocationTree node, int position) {
        ExpressionTree arg = node.getArguments().get(position);
        AnnotatedTypeMirror valueATM = getValueAnnotatedTypeFactory().getAnnotatedType(arg);
        return valueATM.getAnnotation(StringVal.class);
    }
}
