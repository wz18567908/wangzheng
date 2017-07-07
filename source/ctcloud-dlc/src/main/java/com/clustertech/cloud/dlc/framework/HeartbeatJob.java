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

import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;

public final class HeartbeatJob extends SchedulableJob {

    public HeartbeatJob() {
        super("heartbeat", 10);
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String strMasterHost = AppConfig.getInstance().getProperty(ConfigKey.DLC_MASTER_HOST);
        String localhostName = AppConfig.getInstance().getProperty(ConfigKey.HOST_NAME.name());
        if (strMasterHost != null && !strMasterHost.equalsIgnoreCase(localhostName) && !strMasterHost.equalsIgnoreCase("localhost")) {
            DLCListener.getInstance().syncUp(strMasterHost);
        }
    }
}

