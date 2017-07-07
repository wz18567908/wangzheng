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
 * A child class of DataAccessException. If SQLTransformer can transform a
 * SQLException to DataAccessException, it will generate this Exception.
 */
public class IgnorableDataAccessException extends DataAccessException {
    /** Serial version UID */
    private static final long serialVersionUID = -5405885424457166697L;

    public IgnorableDataAccessException() {
        super();
    }

    public IgnorableDataAccessException(String message) {
        super(message);
    }

    public IgnorableDataAccessException(Throwable cause) {
        super(cause);
    }

    public IgnorableDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
