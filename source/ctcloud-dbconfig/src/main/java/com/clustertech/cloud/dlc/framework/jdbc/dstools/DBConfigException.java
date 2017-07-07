/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
 */
package com.clustertech.cloud.dlc.framework.jdbc.dstools;

/**
 * @author qguo
 *
 */
public class DBConfigException extends Exception {

    private static final long serialVersionUID = 4984472601688427938L;

    public DBConfigException() {
        super();
    }

    public DBConfigException(String message) {
        super(message);
    }

    public DBConfigException(Throwable cause) {
        super(cause);
    }

    public DBConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
