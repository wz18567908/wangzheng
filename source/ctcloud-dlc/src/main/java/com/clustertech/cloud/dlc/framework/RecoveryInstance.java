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

import com.clustertech.cloud.dlc.framework.writer.WritingResult;

/**
 * Present required information of a data recovery instance
 */
public class RecoveryInstance {
    private String name;
    private RecoveryStatus status;
    private final Map<String, WritingResult> recWritten = new HashMap<String, WritingResult>();
    private long lastUpdated;
    private Thread thread;

    public enum RecoveryStatus {
        READING, WRITING, IDLE
    }

    public RecoveryInstance(String name) {
        this.name = name;
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

    /**
     * @return the status
     */
    public final RecoveryStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public final void setStatus(RecoveryStatus status) {
        this.status = status;
        this.setLastUpdated(System.currentTimeMillis());
    }

    /**
     * @return the lastUpdated
     */
    public final long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public final void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the recordsWritten
     */
    public final Map<String, WritingResult> getRecWritten() {
        return recWritten;
    }

    /**
     * Add the given written information in the delta into the instance
     * @param delta
     */
    public final void updateRecWritten(Map<String, WritingResult> delta) {
        LoaderHelper.calcRecWritten(this.recWritten, delta);
    }

    /**
     * Reset record written information for all tables
     */
    public final void resetRecWritten() {
        this.recWritten.clear();
    }

    /**
     * @return the thread
     */
    public final Thread getThread() {
        return thread;
    }

    /**
     * @param thread the thread to set
     */
    public final void setThread(Thread thread) {
        this.thread = thread;
    }
}
