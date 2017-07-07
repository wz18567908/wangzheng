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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.writer.DBWriterHelper;
import com.clustertech.cloud.dlc.framework.writer.DataWritingException;

/**
 * The DataRecover class is used for loading PersistedRecords from disk and writing them into database
 */
public final class DataRecovery {
    private static Logger dlcLogger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
    private Set<String> availDataSources = new HashSet<String>();
    private final static long RECOVERY_THREAD_TIMEOUT = 900000; // Timeout in millisecond for a recovery thread

    public void execute() throws DataWritingException {
        int numAvailDs = this.getAvailDataSources();
        if (numAvailDs == 0) {
            // No available data source found.
            dlcLogger.debug("No available data source found. Exiting the data recovery job.");
            return;
        }

        Set<String> backupDirs = this.getBackupDirs();
        if (backupDirs.size() == 0) {
            // No backup directory found.
            dlcLogger.debug("No backup data found. Exiting the data recovery job.");
            return;
        }

        LoaderHelper loaderHelper = LoaderHelper.getInstance();
        // For each backup directory, start a thread
        for (String name : backupDirs) {
            RecoveryInstance recoveryInstance = loaderHelper.getRecoveryInstance(name, false);
            if (recoveryInstance != null
                    && System.currentTimeMillis() - recoveryInstance.getLastUpdated() < RECOVERY_THREAD_TIMEOUT) {
                // Found an existing instance running
                dlcLogger.debug("An existing recovery thread of '{}' is running.", name);
                continue;
            } else if (recoveryInstance != null) {
                // The existing instance is timeout, cancel the current thread
                recoveryInstance.getThread().interrupt();
                recoveryInstance.setLastUpdated(System.currentTimeMillis());
            } else {
                // No existing instance is running, create a new one
                recoveryInstance = new RecoveryInstance(name);
                loaderHelper.addRecoveryInstance(recoveryInstance);
            }

            Runnable recovery = new RecoveryThread(name, this.availDataSources);
            Thread thread = new Thread(recovery);
            recoveryInstance.setThread(thread);

            // Start the recovery thread
            thread.start();
        }
    }

    /**
     * Get all the available data sources
     * @return
     */
    private int getAvailDataSources() {
        HashSet<String> availDs = new HashSet<String>();

        DataSourceHelper dsHelper = DataSourceHelper.getInstance();
        // Loop all datasource configuration
        for (String dsName : dsHelper.getDsManager().getDataSourceConfigs().keySet()) {
            boolean isAvail = dsHelper.checkDbAvail(dsName, dlcLogger);
            if (isAvail == true) {
                availDs.add(dsName);
            }
        }

        int numAvail = availDs.size();

        // Compare the current set to the previous set
        if (numAvail != this.availDataSources.size() || availDs.containsAll(this.availDataSources) == false) {
            // Available data source set has changed
            if (numAvail == 0) {
                dlcLogger.info("No available data source found.");
            } else {
                dlcLogger.info("Found {} available data source(s) {}.", numAvail, availDs);
            }
            this.availDataSources = availDs;
        }

        return numAvail;
    }

    /**
     * Get backup directories
     * @return
     */
    private Set<String> getBackupDirs() {
        HashSet<String> subdirs = new HashSet<String>();

        File dir = new File(DBWriterHelper.backupTop);
        File[] candiateDirs = dir.listFiles();
        if (candiateDirs == null || candiateDirs.length == 0) {
            return subdirs;
        }

        for (File subdir : candiateDirs) {
            if (subdir.isDirectory()) {
                if (subdir.canRead() == false || subdir.canWrite() == false || subdir.canExecute() == false) {
                    dlcLogger.warn("Has no enough previliges to read and write backup data in the directory '{}'",
                            subdir.getAbsolutePath());
                    continue;
                }
                subdirs.add(subdir.getName());
            }
        }

        return subdirs;
    }
}
