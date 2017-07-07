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

import java.util.Map;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.WriterConfig;

/**
 * The interface for data writing plug-in
 */
public abstract class DataWriter {
    /** Logging handler */
    protected Logger logger = null;

    /** Writer config */
    protected WriterConfig config = null;

    /** Name of the loader */
    protected String name = null;

    /**
     * Create an instance with given name, configuration and logging handler
     * @param name
     * @param config
     * @param logger
     */
    public DataWriter(String name, WriterConfig config) {
        this.name = name;
        this.config = config;
        this.logger = LoggerHelper.getInstance().getLogger(
                LoggerType.DATALOADER, this.name);
    }

    /**
     * Will be invoked once every sampling before starting to read data
     * @throws DataWritingException any errors occur
     */
    public abstract void preWrite() throws DataWritingException;

    /**
     * Write data in this function
     * @return a Map to describe how many records having been written for each table
     * @throws DataWritingException any errors occur
     */
    public abstract Map<String, WritingResult> writeData(
            Map<String, RecordList> data) throws DataWritingException;

    /**
     * Will be invoked once every sampling after all data has been written
     * @throws DataWritingException any errors occur
     */
    public abstract void postWrite();

    /**
     * @return the logger
     */
    public final Logger getLogger() {
        return logger;
    }

    /**
     * @return the config
     */
    public final WriterConfig getConfig() {
        return config;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }
}
