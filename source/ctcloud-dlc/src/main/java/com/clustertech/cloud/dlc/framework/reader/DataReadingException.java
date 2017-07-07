/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.reader;

/**
 * Provide information for errors occurs during data reading
 */
public class DataReadingException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = -5791037516105585141L;

    public DataReadingException(String message, Throwable th) {
        super(message, th);
    }
    
    public DataReadingException(Throwable th) {
        super(th);
    }
    
    public DataReadingException(String message) {
        super(message);
    }
}
