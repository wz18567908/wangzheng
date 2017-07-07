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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The WriterConfig describes all required information for a Writer element and it's sub-element 
 * in the dataloader configuration file
 */
public class WriterConfig {
    private ErrorHandler errorHandler = ErrorHandler.DLP;
    private int maxBatchSize = 0;
    private final Map<String, List<SQLConfig>> sqlConfigMap = new HashMap<String, List<SQLConfig>>();
    private String defTable = null;

    public enum ErrorHandler {
        DLP, EXIT
    }

    /**
     * @return the errorHandler
     */
    public final ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * @param errorHandler the errorHandler to set
     */
    public final void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
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

    /**
     * @return the sqlConfig
     */
    public final List<SQLConfig> getSqlConfig(String table) {
        return sqlConfigMap.get(table);
    }

    /**
     * @return the sqlConfigMap
     */
    public final Map<String, List<SQLConfig>> getSqlConfigMap() {
        return sqlConfigMap;
    }

    /**
     * @return the sqlConfig
     */
    public final List<SQLConfig> getDefaultSqlConfig() {
        if (this.defTable == null) {
            return null;
        }
        return sqlConfigMap.get(this.defTable);
    }

    /**
     * @param sqlConfig the sqlConfig to set
     */
    public final void addSqlConfig(SQLConfig sqlConfig) {
        if (this.defTable == null) {
            this.defTable = sqlConfig.getTable();
        }

        List<SQLConfig> list = this.sqlConfigMap.get(sqlConfig.getTable());
        if (list == null) {
            list = new ArrayList<SQLConfig>();
            this.sqlConfigMap.put(sqlConfig.getTable(), list);
        }

        list.add(sqlConfig);
    }
}
