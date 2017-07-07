package com.clustertech.cloud.gui.utils;

public class StringUtil {

    public static String[] trimSplitByComma(String param) {
        return param.trim().split(",");
    }

    public static boolean isNotEmpty(String param) {
        return ! isNullOrEmpty(param);
    }

    public static boolean isNullOrEmpty(String param) {
        if (param == null || param.trim().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getStackTrace(Throwable th) {
        StringBuffer buffException = new StringBuffer();
        buffException.append(String.format("%s%n", th));
        for (StackTraceElement stackTrace : th.getStackTrace()) {
            buffException.append(String.format("\t%s%n", stackTrace));
        }
        return buffException.toString();
    }

    public static String formatErrorLogger(Throwable th, String methodName, String...params) {
        return String.format("Method={%s(%s)}%n%s",
                methodName, formateStringByComma(params), getStackTrace(th));
    }

    public static String formateSqlInData(String commaString) {
        String[] paramArray = trimSplitByComma(commaString);
        return formateStringByComma(paramArray);
    }

    public static String formateSqlInData(String[] params) {
        return formateStringByComma(params);
    }

    public static String formateStringByComma(String...params) {
        StringBuffer buff = new StringBuffer();
        for (String param : params) {
            buff.append("'" + param.trim() + "',");
        }
        return buff.substring(0, buff.length()-1);
    }
}
