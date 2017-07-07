/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.operation;

/**
 * Base exception of operation
 */
public class OperationException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = -4595950421511382589L;

    private boolean retryNeeded = true;

    public OperationException(String message) {
        super(message);
    }

    public OperationException(Throwable th) {
        super(th);
    }

    public OperationException(String message, Throwable th) {
        super(message, th);
    }

    /**
     * @return the retryNeeded
     */
    public final boolean isRetryNeeded() {
        return retryNeeded;
    }

    /**
     * @param retryNeeded the retryNeeded to set
     */
    public final void setRetryNeeded(boolean retryNeeded) {
        this.retryNeeded = retryNeeded;
    }
}
