/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.config;

import java.io.Serializable;

/**
 * The SQLParam describes all required information for a parameter (?) in a SQL statement
 */
public class SQLParam implements Serializable {
    /** Serial version UID */
    private static final long serialVersionUID = -5145495343795917107L;
    private String value;
    private SQLParamType type = SQLParamType.STRING;
    private int maxLen = -1;

    public enum SQLParamType implements Serializable {
        STRING, NUMERIC, TIMESTAMP, TIME, DATE, INTEGER, DOUBLE
    }

    /**
     * @return the src
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value the src to set
     */
    public final void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public final SQLParamType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public final void setType(SQLParamType type) {
        this.type = type;
    }

    /**
     * @return the maxLen
     */
    public final int getMaxLen() {
        return maxLen;
    }

    /**
     * @param maxLen the maxLen to set
     */
    public final void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }
}
