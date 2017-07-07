/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.LoaderInstance.LoaderStatus;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.DLCConfig;
import com.clustertech.cloud.dlc.framework.config.DLCConfigHelper;
import com.clustertech.cloud.dlc.framework.config.LoaderConfig;
import com.clustertech.cloud.dlc.framework.config.LoaderConfigHelper;
import com.clustertech.cloud.dlc.framework.reader.DataReader;
import com.clustertech.cloud.dlc.framework.reader.DataReaderFactory;
import com.clustertech.cloud.dlc.framework.reader.DataReadingException;
import com.clustertech.cloud.dlc.framework.writer.DataWriter;
import com.clustertech.cloud.dlc.framework.writer.DataWritingException;
import com.clustertech.cloud.dlc.framework.writer.DefaultDBWriter;
import com.clustertech.cloud.dlc.framework.writer.WritingResult;

public class DataLoader {
    /** The name of the data loader */
    private String name;

    /** The DataReader instance */
    private DataReader dataReader;

    /** The DataWriter instance */
    private DataWriter dataWriter;

    /** Loader interval in milliseconds */
    private long interval = 0L;

    /** The logging handler */
    private Logger logger = null;

    /** Threshold which records written exceeds, info level message will be logged */
    private final static int DEF_LOGGING_THRESHOLD = 100000;

    /**
     * Create an object of the DataLoader class with a given name
     * @param name
     */
    public DataLoader(String name) {
        this.name = name;
    }

    /**
     * Initialize the data loader
     * @throws LoaderInitializationException
     */
    public void initialize() throws LoaderInitializationException {
        this.logger = LoggerHelper.getInstance().getLogger(LoggerType.DATALOADER, this.name);
        LoaderConfig loaderConfig = LoaderConfigHelper.getInstance().getLoaderConfig(this.name);
        if (loaderConfig == null) {
            throw new LoaderInitializationException(String.format("The loader '%s' does not exists", this.name));
        }

        this.dataReader = DataReaderFactory.createDataReader(this.name, loaderConfig.getReaderConfig());
        this.dataWriter = new DefaultDBWriter(this.name, loaderConfig.getWriterConfig());

        DLCConfig dlcConfig = DLCConfigHelper.getInstance().getDlcConfig(this.name);
        if (dlcConfig == null) {
            throw new LoaderInitializationException(String.format("The loader '%s' does not exists", this.name));
        }
        this.interval = dlcConfig.getInterval() * 1000;
    }

    /**
     * Execute the data loader
     * @throws DataReadingException
     * @throws DataWritingException
     */
    public void execute() {
        LoaderInstance loaderInstance = LoaderHelper.getInstance().getLoaderInstance(this.name);
        try {
            this.logger.info("Starting the loader '{}' ...", name);
            this.executeInternal(loaderInstance);
            this.logger.info("The loader '{}' completed successfully.", this.name);
        } catch (DataReadingException ex) {
            this.logger.error("The loader '{}' exited with reading error: ", this.name, ex);
        } catch (DataWritingException ex) {
            this.logger.error("The loader '{}' exited with writing error: ", this.name, ex);
        } catch (Throwable th) {
            this.logger.error("The loader '{}' exited with uncaught error: ", this.name, th);
        } finally {
            loaderInstance.setStatus(LoaderStatus.IDLE);
        }
    }

    /**
     * Execute the data loader
     * @throws DataReadingException
     * @throws DataWritingException
     */
    private void executeInternal(LoaderInstance loaderInstance) throws DataReadingException, DataWritingException {
        final Map<String, WritingResult> recordWritten = new HashMap<String, WritingResult>();
        int preWrittenTotal = 0;
        int writtenTotal = 0;
        int readTotal = 0;
        boolean firstRound = true;

        // Set the start time for the reader
        long mills = (System.currentTimeMillis() / 1000) * 1000;
        long startTime = this.interval != 0 ? mills - mills % this.interval : mills;
        this.dataReader.setStartTime(startTime);
        this.dataReader.setInterval(this.interval);

        try {
            // preRead
            this.logger.debug("Entering preRead ...");
            loaderInstance.setStatus(LoaderStatus.READING);
            this.dataReader.preRead();

            while (dataReader.hasMoreData()) {
                // readData
                this.logger.debug("Entering readData ...");
                loaderInstance.setStatus(LoaderStatus.READING);
                Map<String, RecordList> records = dataReader.readData();
                if (null == records || records.size() == 0) {
                    // No record to write
                    // Check there has more data to read
                    continue;
                }

                int tmpTotal = this.getReadTotal(records);
                this.logger.debug("Read {} records.", tmpTotal);
                readTotal += tmpTotal;

                if (firstRound) {
                    // preWrite
                    this.logger.debug("Entering preWrite ...");
                    loaderInstance.setStatus(LoaderStatus.WRITING);
                    this.dataWriter.preWrite();

                    firstRound = false;
                }

                Map<String, WritingResult> result = null;
                try {
                    // writeData
                    this.logger.debug("Entering writeData ...");
                    loaderInstance.setStatus(LoaderStatus.WRITING);
                    result = this.dataWriter.writeData(records);
                    writtenTotal = result != null ? LoaderHelper.calcRecWritten(recordWritten, result) : 0;
                } catch (Throwable ex) {
                    // Notify the reader that data failed to be written
                    this.dataReader.onDataWriteFailure();
                    if (ex instanceof DataWritingException) {
                        throw (DataWritingException) ex;
                    } else {
                        throw new DataWritingException(ex);
                    }
                }
                // Notify the reader that data has been written successfully
                this.dataReader.onDataWritten();

                // Calculate total # records written
                // if the # exceeds the threshold, log the # of records written
                int delta = writtenTotal - preWrittenTotal;
                if (delta >= DEF_LOGGING_THRESHOLD) {
                    preWrittenTotal = writtenTotal;
                    this.logger.info(this.formatWrittenInfo(recordWritten));
                    this.logger.info("Read {} records.", readTotal);
                } else {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug(this.formatWrittenInfo(recordWritten));
                    }
                }

                // Update the record written information in the LoaderInstance object
                if (delta > 0) {
                    LoaderInstance instance = LoaderHelper.getInstance().getLoaderInstance(this.name);
                    instance.updateRecWritten(result);
                }
            }
            logger.debug("No more data to read.");
        } finally {
            try {
                // postRead
                this.logger.debug("Entering postRead ...");
                loaderInstance.setStatus(LoaderStatus.CLEANING);
                this.dataReader.postRead();
            } catch (Throwable th) {
                this.logger.error("Unexpected errors occurred in postRead.", th);
            }

            try {
                // postWrite
                this.logger.debug("Entering postWrite ...");
                this.dataWriter.postWrite();
            } catch (Throwable th) {
                this.logger.error("Unexpected errors in postWrite.", th);
            }
        }

        // The latest # of record written has not been logged yet
        if (writtenTotal != preWrittenTotal || writtenTotal == 0) {
            this.logger.info("Read {} records.", readTotal);
            this.logger.info(this.formatWrittenInfo(recordWritten));
        }
    }

    /**
     * For mat record read information
     * @param records
     * @return
     */
    private int getReadTotal(Map<String, RecordList> records) {
        int total = 0;

        for (RecordList recList : records.values()) {
            total += recList.size();
        }
        return total;
    }

    /**
     * Format record written information
     * @param resultMap
     * @return
     */
    private String formatWrittenInfo(Map<String, WritingResult> resultMap) {
        final StringBuffer writtenInfo = new StringBuffer();
        int total = 0;
        for (Entry<String, WritingResult> entry : resultMap.entrySet()) {
            if (writtenInfo.length() > 0) {
                writtenInfo.append(", ");
            }
            total += entry.getValue().getTotal();
            writtenInfo.append(entry.getKey()).append(": ").append(entry.getValue());
        }

        return String.format("Written %s records%s.", total, total > 0 ? " (" + writtenInfo.toString() + ")" : "");
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name) {
        this.name = name;
    }
}
