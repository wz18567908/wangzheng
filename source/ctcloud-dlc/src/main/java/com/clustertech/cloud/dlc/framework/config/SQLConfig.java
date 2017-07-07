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
import java.util.ArrayList;
import java.util.List;

/**
 * The SQLConfig describes all required information for a SQL element and it's sub-element 
 * in the dataloader configuration file
 */
public class SQLConfig implements Serializable {
    /** Serial version UID */
    private static final long serialVersionUID = 740211430325439549L;
    private String ds;
    private String table;
    private String statement;
    private final List<SQLParam> paramList = new ArrayList<SQLParam>();
    private int maxBatchSize = 0;

    /**
     * @return the ds
     */
    public final String getDs() {
        return ds;
    }

    /**
     * @param ds the ds to set
     */
    public final void setDs(String ds) {
        this.ds = ds;
    }

    /**
     * @return the table
     */
    public final String getTable() {
        return table;
    }

    /**
     * @param table the table to set
     */
    public final void setTable(String table) {
        this.table = table;
    }

    /**
     * @return the statement
     */
    public final String getStatement() {
        return statement;
    }

    /**
     * @param statement the statement to set
     */
    public final void setStatement(String statement) {
        this.statement = statement;
    }

    /**
     * @return the paramList
     */
    public final List<SQLParam> getParamList() {
        return paramList;
    }

    /**
     * @param paramList the paramList to set
     */
    public final void addParam(SQLParam param) {
        this.paramList.add(param);
    }

    /**
     * @return the maxBatchSize
     */
    public final int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     * @param maxBatchSize the maxBatchSize to set
     */
    public final void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
}
