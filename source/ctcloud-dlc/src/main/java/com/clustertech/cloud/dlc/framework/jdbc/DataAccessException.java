/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/
package com.clustertech.cloud.dlc.framework.jdbc;

/**
 * Data Access Exception class. It inherit with RuntimeException.
 */
public class DataAccessException extends RuntimeException {
    /** Serial version UID */
    private static final long serialVersionUID = 6562331769755182856L;

    public DataAccessException() {
        super();
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
