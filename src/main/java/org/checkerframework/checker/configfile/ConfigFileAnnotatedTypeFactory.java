package org.checkerframework.checker.configfile;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.configfile.qual.ConfigFile;
import org.checkerframework.checker.configfile.qual.ConfigFileBottom;
import org.checkerframework.checker.configfile.qual.ConfigFileUnknown;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.common.value.qual.BoolVal;
import org.checkerframework.common.value.qual.BottomVal;
import org.checkerframework.common.value.qual.DoubleVal;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.checkerframework.common.value.qual.IntRangeFromNonNegative;
import org.checkerframework.common.value.qual.IntRangeFromPositive;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.qual.PolyValue;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TreeUtils;

public class ConfigFileAnnotatedTypeFactory extends ValueAnnotatedTypeFactory {

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
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        Set<Class<? extends Annotation>> supportedTypeQualifiers =
                super.createSupportedTypeQualifiers();
        Collections.addAll(
                supportedTypeQualifiers,
                ConfigFile.class,
                ConfigFileUnknown.class,
                ConfigFileBottom.class);
        return supportedTypeQualifiers;
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new ConfigFileQualifierHierarchy(factory);
    }

    @Override
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new ConfigFileTransfer((CFAnalysis) analysis);
    }

    private class ConfigFileQualifierHierarchy extends ValueQualifierHierarchy {

        public ConfigFileQualifierHierarchy(MultiGraphFactory factory) {
            super(factory);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if ((AnnotationUtils.areSameByClass(subAnno, ArrayLen.class)
                            || AnnotationUtils.areSameByClass(subAnno, ArrayLenRange.class)
                            || AnnotationUtils.areSameByClass(subAnno, IntVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, IntRange.class)
                            || AnnotationUtils.areSameByClass(subAnno, BoolVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, StringVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, DoubleVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, BottomVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, UnknownVal.class)
                            || AnnotationUtils.areSameByClass(subAnno, IntRangeFromPositive.class)
                            || AnnotationUtils.areSameByClass(
                                    subAnno, IntRangeFromNonNegative.class)
                            || AnnotationUtils.areSameByClass(
                                    subAnno, IntRangeFromGTENegativeOne.class)
                            || AnnotationUtils.areSameByClass(subAnno, PolyValue.class))
                    && (AnnotationUtils.areSameByClass(superAnno, ArrayLen.class)
                            || AnnotationUtils.areSameByClass(superAnno, ArrayLenRange.class)
                            || AnnotationUtils.areSameByClass(superAnno, IntVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, IntRange.class)
                            || AnnotationUtils.areSameByClass(superAnno, BoolVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, StringVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, DoubleVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, BottomVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, UnknownVal.class)
                            || AnnotationUtils.areSameByClass(superAnno, IntRangeFromPositive.class)
                            || AnnotationUtils.areSameByClass(
                                    superAnno, IntRangeFromNonNegative.class)
                            || AnnotationUtils.areSameByClass(
                                    superAnno, IntRangeFromGTENegativeOne.class)
                            || AnnotationUtils.areSameByClass(superAnno, PolyValue.class))) {
                return super.isSubtype(subAnno, superAnno);
            } else if ((AnnotationUtils.areSameByClass(subAnno, ConfigFile.class)
                            || AnnotationUtils.areSameByClass(subAnno, ConfigFileUnknown.class)
                            || AnnotationUtils.areSameByClass(subAnno, ConfigFileBottom.class))
                    && (AnnotationUtils.areSameByClass(superAnno, ConfigFile.class)
                            || AnnotationUtils.areSameByClass(superAnno, ConfigFileUnknown.class)
                            || AnnotationUtils.areSameByClass(superAnno, ConfigFileBottom.class))) {
                if (AnnotationUtils.areSameByClass(subAnno, ConfigFileBottom.class)
                        || AnnotationUtils.areSameByClass(superAnno, ConfigFileUnknown.class)) {
                    return true;
                } else if (AnnotationUtils.areSameByClass(subAnno, ConfigFileUnknown.class)
                        || AnnotationUtils.areSameByClass(superAnno, ConfigFileBottom.class)) {
                    return false;
                } else if (AnnotationUtils.areSameByClass(subAnno, ConfigFile.class)
                        && AnnotationUtils.areSameByClass(superAnno, ConfigFile.class)) {
                    return compareElementValue(subAnno, superAnno);
                } else {
                    throw new BugInCF("We should never reach here.");
                }
            }
            return false;
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
                new ConfigFileTreeAnnotator(this), super.createTreeAnnotator());
    }

    private class ConfigFileTreeAnnotator extends TreeAnnotator {

        public ConfigFileTreeAnnotator(ConfigFileAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
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

                        String propKey = getValueFromStringValAnnoMirror(stringValAnnoMirror);
                        String propValue = readValueFromPropertyFile(propFile, propKey, null);

                        if (propValue != null) {
                            String[] value = {propValue};
                            AnnotationBuilder builder =
                                    new AnnotationBuilder(processingEnv, StringVal.class);
                            builder.setValue("value", value);
                            annotatedTypeMirror.replaceAnnotation(builder.build());
                        }
                    }
                }
            } else if (TreeUtils.isMethodInvocation(
                    node, getPropertyWithDefaultValue, processingEnv)) {
                AnnotationMirror configFileAnnoMirror =
                        atypeFactory.getReceiverType(node).getAnnotation(ConfigFile.class);
                AnnotationMirror stringValAnnoMirrorArg0 =
                        getStringValAnnoMirrorOfArgument(node, 0);
                AnnotationMirror stringValAnnoMirrorArg1 =
                        getStringValAnnoMirrorOfArgument(node, 1);

                if (configFileAnnoMirror != null && stringValAnnoMirrorArg0 != null) {

                    String propFile = getValueFromConfigFileAnnoMirror(configFileAnnoMirror);

                    if (propFile != null) {

                        String propKey = getValueFromStringValAnnoMirror(stringValAnnoMirrorArg0);

                        String defaultValue = null;

                        if (stringValAnnoMirrorArg1 != null) {
                            defaultValue = getValueFromStringValAnnoMirror(stringValAnnoMirrorArg1);
                        }

                        String propValue =
                                readValueFromPropertyFile(propFile, propKey, defaultValue);

                        if (propValue != null) {
                            String[] value = {propValue};
                            AnnotationBuilder builder =
                                    new AnnotationBuilder(processingEnv, StringVal.class);
                            builder.setValue("value", value);
                            annotatedTypeMirror.replaceAnnotation(builder.build());
                        }
                    }
                }
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }

        private String readValueFromPropertyFile(
                String fileName, String argValue, String defaultValue) {
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

                if (defaultValue == null) {
                    res = prop.getProperty(argValue);
                } else {
                    res = prop.getProperty(argValue, defaultValue);
                }
            } catch (Exception e) {
                checker.message(
                        Kind.WARNING, "Exception in ConfigFileChecker.readPropertyFromFile: " + e);
                e.printStackTrace();
            }
            return res;
        }
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
        return this.getAnnotatedType(arg).getAnnotation(StringVal.class);
    }
}
