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

import java.io.Serializable;

import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.SQLConfig;

/**
 * Data to write to disk
 */
public class PersistedRecords implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = -3588061893146237992L;

    private RecordList recList;
    private SQLConfig sqlConfig;

    /**
     * @return the recList
     */
    public final RecordList getRecList() {
        return recList;
    }

    /**
     * @param recList the recList to set
     */
    public final void setRecList(RecordList recList) {
        this.recList = recList;
    }

    /**
     * @return the sqlConfigs
     */
    public final SQLConfig getSqlConfig() {
        return sqlConfig;
    }

    /**
     * @param sqlConfigs the sqlConfigs to set
     */
    public final void setSqlConfigs(SQLConfig sqlConfig) {
        this.sqlConfig = sqlConfig;
    }
}
