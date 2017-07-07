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

import java.util.HashMap;
import java.util.Map;

/**
 * The ReaderConfig describes all required information for a Reader element and it's sub-element 
 * in the dataloader configuration file
 */
public class ReaderConfig {
    private final Map<String, String> properties = new HashMap<String, String>();
    private String readerClass;
    private final Map<String, String[]> headers = new HashMap<String, String[]>();

    private int maxBufferSize = 0;

    /**
     * @return the properties
     */
    public final Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public final void addProperty(String name, String value) {
        this.properties.put(name, value);
    }

    /**
     * @return the readerClass
     */
    public final String getReaderClass() {
        return readerClass;
    }

    /**
     * @param readerClass the readerClass to set
     */
    public final void setReaderClass(String readerClass) {
        this.readerClass = readerClass;
    }

    /**
     * @return the maxBatchSize
     */
    public final int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * @param maxBufferSize the BufferSize to set
     */
    public final void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public Map<String, String[]> getHeaders() {
        return headers;
    }

    public final void addHeader(String name, String[] value) {
        this.headers.put(name, value);
    }
}
