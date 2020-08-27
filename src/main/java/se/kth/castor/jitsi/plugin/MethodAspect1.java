package se.kth.castor.jitsi.plugin;

import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MethodAspect1 {
    @Pointcut(className = "org.jitsi.videobridge.Conference$NoOpDiagnosticContext",
            methodName = "put",
            methodParameterTypes = {"java.lang.String", "java.lang.Object"},
            timerName = "Conference$NoOpDiagnosticContext - put")
    public static class MethodAdvice implements AdviceTemplate {
        private static final int COUNT = 1;

        private static int invocationCount;
        private static final String methodFQN = MethodAdvice.class.getAnnotation(Pointcut.class).className() + "." +
                MethodAdvice.class.getAnnotation(Pointcut.class).methodName();
        private static final TimerName timer = Agent.getTimerName(MethodAdvice.class);
        private static final String transactionType = "Pure";
        private static String receivingObjectFilePath;
        private static String paramObjectsFilePath;
        private static String returnedObjectFilePath;
        private static Logger logger = Logger.getLogger(MethodAdvice.class);

        private static void setup() {
            String[] fileNames = AdviceTemplate.setUpFiles(methodFQN);
            receivingObjectFilePath = fileNames[0];
            paramObjectsFilePath = fileNames[1];
            returnedObjectFilePath = fileNames[2];
        }

        public static synchronized void writeMethodInvocationCount(String objectFilePath) {
            try {
                File file = new File(objectFilePath);
                file.getParentFile().mkdirs();
                FileWriter objectFileWriter = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(objectFileWriter);
                bw.write(methodFQN + ": " + invocationCount);
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                logger.info("MethodAspect" + COUNT);
            }
        }

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindParameterArray Object parameterObjects,
                                          @BindMethodName String methodName) {
            setup();
            ++invocationCount;
            writeMethodInvocationCount(receivingObjectFilePath);
            writeMethodInvocationCount(paramObjectsFilePath);
            MessageSupplier messageSupplier = MessageSupplier.create(
                    "className: {}, methodName: {}",
                    MethodAdvice.class.getAnnotation(Pointcut.class).className(),
                    methodName
            );
            return context.startTransaction(transactionType, methodName, messageSupplier, timer,
                    OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
        }

        @OnReturn
        public static void onReturn(@BindReturn Object returnedObject,
                                    @BindTraveler TraceEntry traceEntry) {
            writeMethodInvocationCount(returnedObjectFilePath);
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                                   @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}