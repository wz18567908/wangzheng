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

/**
 * For every file loader, there is a control file to record
 * the first record, current position of file to read.
 * Every sample load, file loader will read its control file
 * and from the position to read file.
 * This class defined the properties of control file.
 */
public class ControlFileBean {
    private String firstRecord = null;
    private long currentPos = 0;
    private String fileToRead = null;
    private String fileDir = null;

    /**
     * @return the fileToRead
     */
    public String getFileToRead() {
        return fileToRead;
    }

    /**
     * @param fileToRead the fileToRead to set
     */
    public void setFileToRead(String fileToRead) {
        this.fileToRead = fileToRead;
    }

    public void setFirstRecord(String firstrecord) {
        this.firstRecord = firstrecord;
    }

    public String getFirstRecord() {
        return this.firstRecord;
    }

    public void setCurrentPos(long pos) {
        this.currentPos = pos;
    }

    public long getCurrentPos() {
        return this.currentPos;
    }

    /**
     * @param fileDir the fileDir to set
     */
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    /**
     * @return the fileDir
     */
    public String getFileDir() {
        return fileDir;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = System.getProperty("line.separator");
        sb.append(newLine);
        sb.append("FileToRead:" + fileToRead).append(newLine);
        sb.append("FirstRecord:" + firstRecord).append(newLine);
        sb.append("CurrentPosition:" + currentPos).append(newLine);
        sb.append("FileDir:" + this.fileDir).append(newLine);
        return sb.toString();
    }
}
