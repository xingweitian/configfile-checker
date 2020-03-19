package org.checkerframework.checker.configfile;

import static org.checkerframework.checker.configfile.ConfigFileAnnotatedTypeFactory.getValueFromConfigFileAnnoMirror;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.configfile.qual.ConfigFile;
import org.checkerframework.common.value.ValueTransfer;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.TreeUtils;

public class ConfigFileTransfer extends ValueTransfer {

    private final ConfigFileAnnotatedTypeFactory atypeFactory;
    private final ProcessingEnvironment processingEnv;
    private final ExecutableElement propertiesLoad;

    public ConfigFileTransfer(final CFAnalysis analysis) {
        super(analysis);
        this.atypeFactory = (ConfigFileAnnotatedTypeFactory) analysis.getTypeFactory();
        this.processingEnv = atypeFactory.getProcessingEnv();
        this.propertiesLoad =
                TreeUtils.getMethod(
                        java.util.Properties.class.getName(),
                        "load",
                        processingEnv,
                        "java.io.InputStream");
    }

    @Override
    public TransferResult<CFValue, CFStore> visitMethodInvocation(
            MethodInvocationNode n, TransferInput<CFValue, CFStore> in) {

        TransferResult<CFValue, CFStore> transferResult = super.visitMethodInvocation(n, in);

        MethodInvocationTree methodInvocationTree = n.getTree();

        if (TreeUtils.isMethodInvocation(methodInvocationTree, propertiesLoad, processingEnv)) {

            ExpressionTree arg0 = methodInvocationTree.getArguments().get(0);
            AnnotationMirror configFileAnnoMirror =
                    atypeFactory.getAnnotatedType(arg0).getAnnotation(ConfigFile.class);
            String propFile = getValueFromConfigFileAnnoMirror(configFileAnnoMirror);

            if (propFile != null) {

                Node receiver = n.getTarget().getReceiver();
                Receiver receiverRec = FlowExpressions.internalReprOf(atypeFactory, receiver);
                configFileAnnoMirror = atypeFactory.createConfigFileAnnoMirror(propFile);

                if (transferResult.containsTwoStores()) {
                    CFStore thenStore = transferResult.getThenStore();
                    CFStore elseStore = transferResult.getElseStore();
                    thenStore.insertValue(receiverRec, configFileAnnoMirror);
                    elseStore.insertValue(receiverRec, configFileAnnoMirror);
                } else {
                    CFStore regularStore = transferResult.getRegularStore();
                    regularStore.insertValue(receiverRec, configFileAnnoMirror);
                }
            }
        }
        return transferResult;
    }
}
