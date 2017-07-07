/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.writer;

/**
 * Provide information for errors occurs during data writing
 */
public class DataWritingException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = 1348481642202907444L;

    public DataWritingException(String message, Throwable th) {
        super(message, th);
    }

    public DataWritingException(Throwable th) {
        super(th);
    }

    public DataWritingException(String message) {
        super(message);
    }
}
