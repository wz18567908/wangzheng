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

/**
 * The DataLoaderConfig contains all information of a dataloader configuration
 */
public class LoaderConfig {
    private String description;
    private ReaderConfig readerConfig;
    private WriterConfig writerConfig;
    private int maxBufferSize;

    /**
     * @return the description
     */
    public final String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the readerConfig
     */
    public final ReaderConfig getReaderConfig() {
        return readerConfig;
    }
    /**
     * @param readerConfig the readerConfig to set
     */
    public final void setReaderConfig(ReaderConfig readerConfig) {
        this.readerConfig = readerConfig;
    }
    /**
     * @return the writerConfig
     */
    public final WriterConfig getWriterConfig() {
        return writerConfig;
    }
    /**
     * @param writerConfig the writerConfig to set
     */
    public final void setWriterConfig(WriterConfig writerConfig) {
        this.writerConfig = writerConfig;
    }
    /**
     * @return the maxBufferSize
     */
    public final int getMaxBufferSize() {
        return maxBufferSize;
    }
    /**
     * @param maxBufferSize the maxBufferSize to set
     */
    public final void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }
}
