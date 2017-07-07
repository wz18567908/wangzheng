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

import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;

public abstract class SchedulableJob implements Job {
    private final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

    protected static Scheduler scheduler = null;

    protected String jobName;
    protected int interval;
    public SchedulableJob(String jobName, int interval) {
        this.jobName = jobName;
        this.interval = interval;
    }
    public void schedule() throws SchedulerException {
        logger.info("Scheduling the job:" + jobName);

        // Create the JobDetail instance.
        JobDetail job = JobBuilder.newJob(this.getClass()).withIdentity(jobName).build();

        // Trigger the job to run on the next round minute
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName).startNow()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(interval)).build();

        if (scheduler == null) {
            this.initialize();
        }
        scheduler.scheduleJob(job, trigger);
        logger.info("done.");
    }

    /**
     * Initialize the scheduler
     * @throws SchedulerException
     */
    public void initialize() throws SchedulerException {
        Properties prop = new Properties();
        prop.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.setProperty("org.quartz.threadPool.threadCount", "10");
        prop.setProperty("org.quartz.threadPool.threadPriority", "5");
        prop.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        SchedulerFactory sf = new StdSchedulerFactory(prop);
        scheduler = sf.getScheduler();
        scheduler.start();
    }

    /**
     * Shutdown the job scheduler
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
        } catch (Throwable th) {
            // Do nothing
        }
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
    }
}
