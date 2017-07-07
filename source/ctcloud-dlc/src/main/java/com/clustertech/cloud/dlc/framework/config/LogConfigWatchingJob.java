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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.clustertech.cloud.dlc.framework.SchedulableJob;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.IConfigUpdateListener.ConfigType;

/**
 * The LogConfigWatchingJob watch the log4j.properties file.
 * If the file get update, reconfigure the log4j
 */
public final class LogConfigWatchingJob extends SchedulableJob {
    private Map<ConfigType, Long> lastModifiedMap = new HashMap<ConfigType, Long>();

    public LogConfigWatchingJob() {
        super("log-config-watchdog", 3600);
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        // Check log properties
        long lastModified = LoggerHelper.getInstance().getConfLastModified();
        Long knownLastModified = lastModifiedMap.get(ConfigType.LOG_PROPERTIES);

        if (knownLastModified != null && knownLastModified < lastModified) {
            // Log properties modified
            AppConfig.getInstance().fireConfigUpdate(ConfigType.LOG_PROPERTIES);
        }

        if (knownLastModified == null || knownLastModified < lastModified) {
            lastModifiedMap.put(ConfigType.LOG_PROPERTIES, lastModified);
        }
    }
}
