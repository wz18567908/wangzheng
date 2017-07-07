package com.clustertech.cloud.jni.lsf;

public class LSFBatchException extends RuntimeException {
    private static final long serialVersionUID = 8780222902518948262L;

    public LSFBatchException() {
    }

    public LSFBatchException(String message) {
        super(message);
    }
}
