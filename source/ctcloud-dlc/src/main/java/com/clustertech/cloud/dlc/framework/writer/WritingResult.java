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

/**
 * The WritingResult to store number of records written by data writer
 */
public class WritingResult implements Cloneable {
    private int numWrittenToTable = 0;
    private int numWrittenToDisk = 0;
    private int numIgnored = 0;

    /**
     * Create an instance of WritingResult with given numbers
     * @param numWrittenToTable
     * @param numWrittenToDisk
     * @param numIgnored
     */
    public WritingResult(int numWrittenToTable, int numWrittenToDisk, int numIgnored) {
        this.numWrittenToTable = numWrittenToTable;
        this.numWrittenToDisk = numWrittenToDisk;
        this.numIgnored = numIgnored;
    }

    /**
     * @return the numWrittenToTable
     */
    public final int getNumWrittenToTable() {
        return numWrittenToTable;
    }

    /**
     * @param numWrittenToTable the numWrittenToTable to set
     */
    public final void setNumWrittenToTable(int numWrittenToTable) {
        this.numWrittenToTable = numWrittenToTable;
    }

    /**
     * @return the numWrittenToDisk
     */
    public final int getNumWrittenToDisk() {
        return numWrittenToDisk;
    }

    /**
     * @param numWrittenToDisk the numWrittenToDisk to set
     */
    public final void setNumWrittenToDisk(int numWrittenToDisk) {
        this.numWrittenToDisk = numWrittenToDisk;
    }

    /**
     * @return the numIgnored
     */
    public final int getNumIgnored() {
        return numIgnored;
    }

    /**
     * @param numIgnored the numIgnored to set
     */
    public final void setNumIgnored(int numIgnored) {
        this.numIgnored = numIgnored;
    }

    /**
     * Get total number of records processed
     * @return
     */
    public final int getTotal() {
        return this.numWrittenToTable + this.numWrittenToDisk + this.numIgnored;
    }

    /**
     * Add the given result to the current one
     * @param result
     */
    public final WritingResult add(WritingResult result) {
        this.numWrittenToTable += result.numWrittenToTable;
        this.numWrittenToDisk += result.numWrittenToDisk;
        this.numIgnored += result.numIgnored;
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getTotal());

        if (this.numWrittenToDisk > 0 || this.numIgnored > 0) {
            buffer.append(" (").append(this.numWrittenToTable).append(" written into database");
            if (this.numWrittenToDisk > 0) {
                buffer.append(", ").append(this.numWrittenToDisk).append(" written into file");
            }
            if (this.numIgnored > 0) {
                buffer.append(", ").append(this.numIgnored).append(" ignored");
            }
            buffer.append(")");
        }

        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public WritingResult clone() {
        try {
            return (WritingResult) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
