/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework;

/**
 * Provide detailed information for the error occurs during loader initialization
 */
public class LoaderInitializationException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = 4538694769995243959L;

    public LoaderInitializationException(String msg) {
        super(msg);
    }

    public LoaderInitializationException(Throwable cause) {
        super(cause);
    }
}
