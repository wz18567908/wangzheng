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

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.DLCConfig;
import com.clustertech.cloud.dlc.framework.config.DLCConfigHelper;

/**
 * The job will be scheduled by system and call data loaders to read, write data
 */
@DisallowConcurrentExecution
public class DataLoaderJob extends SchedulableJob {

    /**
     * Real DataLoader instance.
     */
    private DataLoader dataloader = null;

    public final static String DATALOADER_KEY = "DATALOADER_KEY";
    private final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
    public DataLoaderJob() {
        super("data-loader", 300);
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // invoke the template method of DataLoader instance.
        Logger dlcLogger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

        try {
            // Get JobDataMap instance
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            // Get DataLoader instance from JobDataMap
            this.dataloader = (DataLoader) dataMap.get(DATALOADER_KEY);

            // Check the DataLoader instance. If can not found, it will throw a
            // JobExecutionException.
            if (this.dataloader == null) {
                dlcLogger.error("Failed to get the instance of the dataloader");
            }

            dataloader.execute();
        } catch (Throwable ex) {
            dlcLogger.error("Failed to load data.", ex);
        }
    }

    /**
     * @see com.clustertech.report.dlc.framework.commons.JobHelper#schedule()
     */
    @Override
    public void schedule() throws SchedulerException {
        DLCConfigHelper dlcHelper = DLCConfigHelper.getInstance();

        for (DLCConfig dlcConfig : dlcHelper.getDlcConfigMap().values()) {
            if (!(dlcConfig.isEnabled())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The loader '{}' is not enabled.", dlcConfig.getName());
                }
                continue;
            }

            String name = dlcConfig.getName();
            try {
                logger.info("Scheduling the loader '{}' ... ", name);
                // Create the JobDetail instance.
                JobDetail job = JobBuilder.newJob(DataLoaderJob.class).withIdentity(name).build();
                DataLoader dataloader = new DataLoader(name);
                dataloader.initialize();

                // Get the JobDataMap instance and put the current DataLoader instance into it.
                JobDataMap dataMap = job.getJobDataMap();
                dataMap.put(DataLoaderJob.DATALOADER_KEY, dataloader);

                // Trigger the job to run on the next round minute
                Trigger trigger = null;
                if (dlcConfig.getCron() == null) {
                    long mills = System.currentTimeMillis();
                    long interval = dlcConfig.getInterval() * 1000;
                    long gap = interval - mills % interval;
                    logger.trace("Mills to wait for starting the first sampling {}.", gap);
                    trigger = TriggerBuilder.newTrigger().withIdentity(name).startAt(new Date(mills + gap))
                            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(dlcConfig.getInterval())).build();
                } else {
                    trigger = TriggerBuilder.newTrigger().withIdentity(name)
                            .withSchedule(CronScheduleBuilder.cronSchedule(dlcConfig.getCron())).build();
                }
                if (scheduler == null) {
                    this.initialize();
                }
                scheduler.scheduleJob(job, trigger);
            } catch (Throwable th) {
                logger.error("Failed to schedule the loader '{}'.", name, th);
            }
        }
    }

}
