package com.clustertech.cloud.gui.exception;

public class CTCloudException extends Exception {
    private static final long serialVersionUID = -4929280785105915515L;

    public CTCloudException(String msg) {
        super(msg);
    }

    public CTCloudException(String msg, Throwable cause) {
        super(msg,cause);
    }

    public CTCloudException(Throwable cause) {
        super(cause);
    }
}
