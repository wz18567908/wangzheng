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

import java.io.Serializable;

/**
 * The base class of all system operation, e.g. ping, shutdown daemon, recovery, and etc.
 */
public class OperationBase implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1357460758336527171L;

    protected OperationType operation = null;
    protected String sponser = null;

    public OperationBase(OperationType operation) {
        this.operation = operation;
    }

    /**
     * @return the operation
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    /**
     * @return the fromHost
     */
    public final String getSponser() {
        return sponser;
    }

    /**
     * @param sponser the fromHost to set
     */
    public final void setSponser(String sponser) {
        this.sponser = sponser;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OperationBase [operation=" + operation + ", sponser=" + sponser + "]";
    }
}
