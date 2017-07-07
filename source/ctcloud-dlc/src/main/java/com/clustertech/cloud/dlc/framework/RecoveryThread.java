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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.RecoveryInstance.RecoveryStatus;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.SQLConfig;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceConfig;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.writer.DBWriterHelper;
import com.clustertech.cloud.dlc.framework.writer.PersistedRecords;
import com.clustertech.cloud.dlc.framework.writer.WritingResult;

/**
 * A thread to load serialized PersistedRecords object from disk and load it into database
 */
public class RecoveryThread implements Runnable {
    /** Name of the thread */
    private String name;

    /** Currently available data sources */
    private Set<String> availDataSources;
    /** File list */
    private List<SortableFile> fileList;
    /** Logger handler */
    private Logger logger;

    /**
     * Create a RecoveryThread instance with the given name
     * @param name
     */
    public RecoveryThread(String name, Set<String> availDataSources) {
        this.name = name;
        this.availDataSources = availDataSources;
        this.fileList = new LinkedList<SortableFile>();
        this.logger = LoggerHelper.getInstance().getLogger(LoggerType.DATALOADER, this.name);
    }

    /**
     * Get file list on which files need to be loaded into database
     */
    private void getFileList() {
        File dir = new File(DBWriterHelper.backupTop + File.separator + this.name);
        DataSourceConfig dsConfig = DataSourceHelper.getInstance().getDsManager().getDefaultDataSourceConfig();
        // Check if the default data source is available
        final boolean isDefaultAvail = dsConfig == null ? false : this.availDataSources.contains(dsConfig.getName());

        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (name.endsWith(DBWriterHelper.backupFileSuffix) == false) {
                    return false;
                }

                // Get the data source name from the file
                int pos1 = name.indexOf('.');
                pos1 = name.indexOf('.', pos1 + 1);
                if (pos1 < 0) {
                    return false;
                }

                int pos2 = name.lastIndexOf('.');
                if (pos2 == pos1) {
                    return isDefaultAvail == true;
                }

                String dsName = name.substring(pos1 + 1, pos2);
                return availDataSources.contains(dsName);
            }
        });

        if (this.logger.isDebugEnabled()) {
            logger.debug("Found {} file(s) to recover.", files == null ? 0 : files.length);
        }

        if (files == null || files.length == 0) {
            return;
        }

        // Loop all files
        for (File file : files) {
            String name = file.getName();
            int pos = name.indexOf('.');
            try {
                long timestamp = Long.parseLong(name.substring(0, pos));
                this.fileList.add(new SortableFile(file, timestamp));
            }
            catch (Exception ex) {
                this.logger.debug("Skip the file with invalid name '{}'.", name);
            }
        }

        // Sort the file in ascending order
        Collections.sort(this.fileList);
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        FileInputStream fileInputStream = null;
        File curFile;
        // Flag indicates the recovery thread got canceled externally
        boolean canceled = false;
        DBWriterHelper dbWriterHelper = new DBWriterHelper(this.logger);

        try {
            this.getFileList();
            RecoveryInstance recoveryInstance = LoaderHelper.getInstance().getRecoveryInstance(this.name, true);
            for (SortableFile sortableFile : this.fileList) {
                curFile = sortableFile.getFile();

                if (Thread.interrupted()) {
                    canceled = true;
                    break;
                }

                // Set the status to reading
                recoveryInstance.setStatus(RecoveryStatus.READING);
                // Read PersistedRecords from disk
                fileInputStream = new FileInputStream(curFile);
                ObjectInputStream input = new ObjectInputStream(fileInputStream);
                PersistedRecords persistedRecords = (PersistedRecords) input.readObject();
                if (persistedRecords == null) {
                    this.logger.error("Failed to read the file '{}'. Backup the file.", curFile.getName());
                    FileUtils.moveFile(curFile, new File(curFile.getAbsolutePath() + ".bak"));
                }

                // Close the input stream
                try {
                    fileInputStream.close();
                    fileInputStream = null;
                } catch (Throwable th) {
                    // Ignore
                }

                if (Thread.interrupted()) {
                    canceled = true;
                    break;
                }
                recoveryInstance.setStatus(RecoveryStatus.WRITING);
                SQLConfig sqlConfig = persistedRecords.getSqlConfig();
                // Write data into database
                WritingResult result = dbWriterHelper.writeRecordsFromDLP(persistedRecords.getRecList(), sqlConfig);
                int total = result.getTotal();
                StringBuffer writtenInfo = new StringBuffer();
                if (total > 0) {
                    writtenInfo.append(" (").append(sqlConfig.getTable()).append("@").append(sqlConfig.getDs())
                            .append(": ").append(result.toString()).append(")");
                }
                this.logger.info("Recovered {} records{}.", total, writtenInfo.toString());
                // Loaded the file ok, then delete the file.
                FileUtils.forceDelete(sortableFile.getFile());
            }
        } catch (Throwable th) {
            this.logger.error("Failed to recover the file '{}'.", th);
        } finally {
            if (canceled == false) {
                LoaderHelper.getInstance().removeRecoveryInstance(this.name);
            }

            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable th) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Sortable file
     */
    private class SortableFile implements Comparable<SortableFile> {
        private File file;
        private long timestamp;

        public SortableFile(File file, long timestamp) {
            this.file = file;
            this.timestamp = timestamp;
        }

        /**
         * @return the file
         */
        public final File getFile() {
            return file;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(SortableFile file) {
            if (this.timestamp < file.timestamp) {
                return -1;
            } else if (this.timestamp > file.timestamp) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
