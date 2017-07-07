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

import java.util.Map;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.ReaderConfig;

/**
 * The abstract class for data reading plug-in
 */
public abstract class DataReader {
    /** Logging handler */
    protected Logger logger = null;

    /** Reader config */
    protected ReaderConfig config = null;

    /** Name of the data loader */
    protected String name = null;

    /** Start time */
    private long startTime = 0L;

    /** Interval */
    private long interval = 0L;

    /**
     * Create an instance with given name, configuration and logging handler
     * @param name
     * @param config
     * @param logger
     */
    public DataReader(String name, ReaderConfig config) {
        this.name = name;
        this.config = config;
        this.logger = LoggerHelper.getInstance().getLogger(LoggerType.DATALOADER, name);
    }

    /**
     * Will be invoked once every sampling before starting to read data
     * @throws DataReadingException any errors occur
     */
    public void preRead() throws DataReadingException{
    }

    /**
     * Will be invoked before readData to determine if there is more data to read
     * @return return true, if there is more data to read, otherwise, return false
     * @throws DataReadingException any errors occur
     */
    public abstract boolean hasMoreData() throws DataReadingException;

    /**
     * Read data in this function
     * @return A RecordList map. Each RecoradList contains a list of records for a given table
     * @throws DataReadingException any errors occur
     */
    public abstract Map<String, RecordList> readData() throws DataReadingException;

    /**
     * If data returned by readData has been written successfully,
     * this function will be invoked
     */
    public void onDataWritten() {
    }

    /**
     * If data returned by readData failed to be written,
     * this function will be invoked
     */
    public void onDataWriteFailure() {
    }

    /**
     * Will be invoked once every sampling after all data has been read.
     * Usually, it is used for cleaning up purpose
     * @throws DataReadingException any errors occur
     */
    public void postRead() {
    }

    /**
     * @return the logger
     */
    public final Logger getLogger() {
        return logger;
    }

    /**
     * @return the config
     */
    public final ReaderConfig getConfig() {
        return config;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the startTime
     */
    public final long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public final void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
