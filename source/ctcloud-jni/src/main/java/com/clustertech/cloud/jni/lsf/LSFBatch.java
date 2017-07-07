package com.clustertech.cloud.jni.lsf;

import java.util.Arrays;
import java.util.List;

import com.clustertech.cloud.jni.lsf.domian.LSFJobInfo;

public final class LSFBatch {
    public static final String LSF_APPLICATION_NAME = "lsf.cluster.cluster1";

    private static LSFBatch ourInstance;

    public static LSFBatch getInstance() {
        if (ourInstance == null) {
            System.loadLibrary("lsb4j");
            ourInstance = new LSFBatch(LSF_APPLICATION_NAME);
        }
        return ourInstance;
    }

    private LSFBatch(String appName) {
        init(appName);
    }

    private native void init(String appName);

    public native LSFJobInfo[] readJobInfo(LSFJobInfo job, long jobId, String jobName,
            String userName, String queueName, String hostName, int options);

    public synchronized List<LSFJobInfo> readJobInfo(long jobId) throws LSFBatchException {
        LSFJobInfo[] jobInfos = null;
        try {
            jobInfos = readJobInfo(null, jobId, null, "all", null, null, 0x0001);
            if (jobInfos == null || jobInfos.length <= 0) {
                return null;
            }
        } catch (Throwable th) {
            throw new LSFBatchException("the jobId [" + jobId + "] is invalid.");
        }
        return Arrays.asList(jobInfos);
    }
}
