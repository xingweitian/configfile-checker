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
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TreeUtils;

public class ConfigFileAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    private final AnnotationMirror CONFIGFILE =
            AnnotationBuilder.fromClass(elements, ConfigFile.class);
    private final AnnotationMirror CONFIGFILEUNKNOWN =
            AnnotationBuilder.fromClass(elements, ConfigFileUnknown.class);
    private final AnnotationMirror CONFIGFILEBOTTOM =
            AnnotationBuilder.fromClass(elements, ConfigFileBottom.class);

    private final ExecutableElement getResourceAsStream =
            TreeUtils.getMethod(
                    java.lang.ClassLoader.class.getName(), "getResourceAsStream", 1, processingEnv);

    private final ExecutableElement propertiesLoad =
            TreeUtils.getMethod(
                    java.util.Properties.class.getName(),
                    "load",
                    processingEnv,
                    "java.io.InputStream");

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
        return new ConfigFileQualifierHierarchy(factory, CONFIGFILEBOTTOM);
    }

    private class ConfigFileQualifierHierarchy extends GraphQualifierHierarchy {

        public ConfigFileQualifierHierarchy(MultiGraphFactory factory, AnnotationMirror bottom) {
            super(factory, bottom);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSame(subAnno, CONFIGFILEBOTTOM)
                    || AnnotationUtils.areSame(superAnno, CONFIGFILEUNKNOWN)) {
                return true;
            } else if (AnnotationUtils.areSame(subAnno, CONFIGFILEUNKNOWN)
                    || AnnotationUtils.areSame(superAnno, CONFIGFILEBOTTOM)) {
                return false;
            } else if (AnnotationUtils.areSame(subAnno, CONFIGFILE)
                    && AnnotationUtils.areSame(superAnno, CONFIGFILE)) {
                return compareConfigFileTypeValue(subAnno, superAnno);
            } else {
                throw new BugInCF("We should never reach here.");
            }
        }

        private boolean compareConfigFileTypeValue(
                AnnotationMirror subAnno, AnnotationMirror superAnno) {
            String subAnnoTypeValue =
                    AnnotationUtils.getElementValue(subAnno, "value", String.class, false);
            String superAnnoTypeValue =
                    AnnotationUtils.getElementValue(superAnno, "value", String.class, false);
            return superAnnoTypeValue.equals(subAnnoTypeValue);
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
            String fileName;

            AnnotatedTypeMirror configFileATM = atypeFactory.getAnnotatedType(node);
            AnnotationMirror configFileAnnoMirror = configFileATM.getAnnotation(ConfigFile.class);

            ExpressionTree arg0 = node.getArguments().get(0);
            ValueAnnotatedTypeFactory valueCheckerATF = getValueAnnotatedTypeFactory();
            AnnotatedTypeMirror valueATM = valueCheckerATF.getAnnotatedType(arg0);
            AnnotationMirror stringValAnnoMirror = valueATM.getAnnotation(StringVal.class);

            if (TreeUtils.isMethodInvocation(node, getResourceAsStream, processingEnv)) {
                if (stringValAnnoMirror == null) {
                    return super.visitMethodInvocation(node, annotatedTypeMirror);
                }
                fileName = getValueFromStringValAnnoMirror(stringValAnnoMirror);
                if (fileName != null) {
                    configFileAnnoMirror = createAnnotation(fileName, ConfigFile.class);
                    annotatedTypeMirror.replaceAnnotation(configFileAnnoMirror);
                }
            } else if (TreeUtils.isMethodInvocation(node, propertiesLoad, processingEnv)
                    && configFileAnnoMirror != null) {
                fileName = getValueFromConfigFileAnnoMirror(configFileAnnoMirror);
                if (fileName != null) {
                    AnnotatedTypeMirror receiverATM = atypeFactory.getReceiverType(node);
                    configFileAnnoMirror = createAnnotation(fileName, ConfigFile.class);
                    receiverATM.replaceAnnotation(configFileAnnoMirror);
                }
            } else if (TreeUtils.isMethodInvocation(node, getProperty, processingEnv)) {
                AnnotatedTypeMirror receiverATM = atypeFactory.getReceiverType(node);
                configFileAnnoMirror = receiverATM.getAnnotation(ConfigFile.class);
                if (configFileAnnoMirror != null && stringValAnnoMirror != null) {
                    fileName = getValueFromConfigFileAnnoMirror(configFileAnnoMirror);
                    if (fileName != null) {
                        String argValue = getValueFromStringValAnnoMirror(stringValAnnoMirror);
                        String propertyValue = readPropertyFromFile(fileName, argValue);
                        if (propertyValue != null) {
                            annotatedTypeMirror.replaceAnnotation(
                                    createAnnotation(fileName, StringVal.class));
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
                    return res;
                }

                prop.load(in);
                res = prop.getProperty(argValue);
            } catch (Exception e) {
                checker.message(
                        Kind.WARNING, "Exception in PropertyKeyChecker.keysOfPropertyFile: " + e);
                e.printStackTrace();
            }
            return res;
        }
    }

    private ValueAnnotatedTypeFactory getValueAnnotatedTypeFactory() {
        return getTypeFactoryOfSubchecker(ValueChecker.class);
    }

    private AnnotationMirror createAnnotation(String value, Class<? extends Annotation> className) {
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

    private String getValueFromConfigFileAnnoMirror(AnnotationMirror configFileAnnoMirror) {
        return AnnotationUtils.getElementValue(configFileAnnoMirror, "value", String.class, false);
    }
}
