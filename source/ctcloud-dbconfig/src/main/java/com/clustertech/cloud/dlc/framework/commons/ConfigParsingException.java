/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.commons;

/**
 * An exception provide information on configuration parsing error.
 */
public final class ConfigParsingException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = 4409499470627786187L;

    private String fileName = null;

    public ConfigParsingException(String fileName, Throwable ex) {
        super("Fail to parse " + fileName, ex);
        this.fileName = fileName;
    }

    public ConfigParsingException (String fileName, String errMsg) {
        super(errMsg);
        this.fileName = fileName;
    }

    public ConfigParsingException (String errMsg) {
        super(errMsg);
    }

    /**
     * @return the fileName
     */
    public final String getFileName() {
        return fileName;
    }
}
