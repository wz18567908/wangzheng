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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;

/**
 * The job will be scheduled by system and recover data persisted on disk into database
 */
public class DataRecoveryJob extends SchedulableJob {
    public DataRecoveryJob() {
        super("data-recovery", 3600);
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // invoke the template method of DataLoader instance.
        Logger dlcLogger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

        try {
            dlcLogger.debug("Starting the data recovery job ...");
            DataRecovery dataRecovery = new DataRecovery();
            dataRecovery.execute();
            dlcLogger.debug("The data recovery job completed.");
        } catch (Throwable ex) {
            dlcLogger.error("Failed to recover data.", ex);
        }
    }
}
