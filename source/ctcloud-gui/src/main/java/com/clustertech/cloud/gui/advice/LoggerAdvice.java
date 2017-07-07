package com.clustertech.cloud.gui.advice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.clustertech.cloud.gui.utils.StringUtil;

public class LoggerAdvice {
    private Logger logger = Logger.getLogger(this.getClass());

    public void debugLog(JoinPoint joinPoint) {
        log(joinPoint, null);;
    }

    public void exceptionLog(JoinPoint joinPoint, Throwable exception) {
        log(joinPoint, exception);
    }

    private void log(JoinPoint joinPoint, Throwable exception) {
        Signature signature = joinPoint.getSignature();
        Class<? extends Object> targetClass = joinPoint.getTarget().getClass();
        String realClassName = targetClass.getSimpleName();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        Method realMethod = null;
        try {
            realMethod = targetClass.getDeclaredMethod(signature.getName(),
                    targetMethod.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                realMethod = targetClass.getSuperclass().getDeclaredMethod(signature.getName(),
                        targetMethod.getParameterTypes());
            } catch (NoSuchMethodException | SecurityException ex) {
                logger.error(ex);
            }
        }

        Object[] args = joinPoint.getArgs();
        String methodName = realMethod.getName();
        Class<?>[] parameterTypes = realMethod.getParameterTypes();
        Map<String, String> paramMap = new HashMap<String, String>();
        for (int i = 0; i < parameterTypes.length; i++) {
            String paramName = parameterTypes[i].getSimpleName();
            String paramValue = null;
            if (args[i] != null) {
                paramValue = args[i].toString();
            }
            paramMap.put(paramName, paramValue);
        }

        String formatParam = "";
        if (parameterTypes.length > 0) {
            String params = paramMap.toString();
            formatParam = params.substring(1, params.length() -1);
        }

        if (exception == null) {
            logger.debug(String.format("The method %s.%s(%s) is executed.",
                    realClassName, methodName, formatParam));
        } else {
            logger.error(String.format("Faild to execute the method %s.%s(%s) due to: %s",
                    realClassName, methodName, formatParam, StringUtil.getStackTrace(exception)));
        }
    }
}
