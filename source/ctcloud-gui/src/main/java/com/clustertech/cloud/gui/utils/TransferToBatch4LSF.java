package com.clustertech.cloud.gui.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.domain.job.LSFJobEntity;

public class TransferToBatch4LSF implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static String PREFIX = "jobEnv_";

    private static final char[] list = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9' };

    public static String createRandomChar(int capacity) throws Exception {
        StringBuffer sb = new StringBuffer(capacity);
        char randomChar = ' ';
        do {
            randomChar = list[new SecureRandom().nextInt(list.length)];
            sb.append(randomChar);
        } while (sb.indexOf(String.valueOf(randomChar)) != -1 && sb.length() < capacity);
        return sb.toString();
    }

    public static Map<String, String> getBatchEnv(LSFJobEntity job) {
        Map<String, String> envParams = new HashMap<>();
        Field[] fields = LSFJobEntity.class.getDeclaredFields();
        for (Field property : fields) {
            String pName = property.getName();
            if (!pName.equals("serialVersionUID")) {
                try {
                    Object pValue = PropertyUtils.getProperty(job, pName);
                    if (null != pValue) {
                        envParams.put(PREFIX + pName.toUpperCase(), pValue.toString());
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    Logger logger = Logger.getLogger(TransferToBatch4LSF.class);
                    logger.error(StringUtil.formatErrorLogger(e, "getBatchEnv", String.format("LSFJobEntity(%s,%s,%s,%s)",
                                    job.getJobName(), job.getCommand(), job.getUserName(), job.getQueue())));
                }
            }
        }
        return envParams;
    }
}
