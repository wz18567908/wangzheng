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

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.Daemon;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.DLCConfigHelper;
import com.clustertech.cloud.dlc.framework.config.LoaderConfigHelper;
import com.clustertech.cloud.dlc.framework.config.LogConfigWatchingJob;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;

/**
 * The entry class of the Data Loader Controller
 */
public final class DLController extends Daemon {

    protected final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
    private final static String usageCmd = "dlc";
    private static DLController controller = null;
    private final static int EXIT_CODE_SHUTDOWN = 100;
    private final static int MAX_RETRIES = 10;
    private final static int RETRY_INTERVAL = 2000; // in milliseconds

    /**
     * Create an empty instance
     */
    private DLController() {
        super(logger);
    }

    /**
     * Get the instance of the Coordinator class
     * @return
     */
    public final synchronized static DLController getInstance() {
        if (controller == null) {
            controller = new DLController();
        }

        return controller;
    }

    /**
     * Entry of the master
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            realMain(args);
        } catch (Throwable th) {
            logger.error("Uncaught exception occurs.", th);
        }
    }

    /**
     * @param args command-line arguments
     */
    private static void realMain(String[] args) {
        AppConfig.getInstance().initialize();

        DLController controller = DLController.getInstance();
        logger.info(getVMInfo());

        try {
            controller.parseCommandLine(args);
        } catch (Exception ex) {
            controller.usage(usageCmd, false);
            logger.error("Failed to parse command line. Exiting the system ...", ex);
            System.exit(-1);
        }

        if (controller.operation == CmdOption.START) {
            try {
                controller.startEcosystem();
            } catch (Throwable th) {
                logger.error(th.getMessage(), th);
                DLController.getInstance().shutdown();
                logger.error("Exiting the system ... done.");
                System.exit(-1);
            }
        } else {
            try {
                String hostName = AppConfig.getInstance().getProperty(ConfigKey.HOST_NAME);
                int port = Integer.parseInt(AppConfig.getInstance().getProperty(ConfigKey.DLC_PORT));

                Acknowledgment ack = controller.shutdown(hostName, port);
                if (ack != null && ack.isSuccessful()) {
                    logger.info("The master has been shutdown sucessfully.");
                } else {
                    String message = ack == null ? "communication timeout" : ack.getMessage();
                    logger.error("Failed to stop the master (host: {} port: {}) ({}).", new Object[] { hostName, port,
                            message });
                }
            } catch (Exception ex) {
                logger.error("Failed to shutdown the master.", ex);
            }
        }
    }

    /**
     * Start essential jobs and listener
     */
    public void startEcosystem() throws Exception {
        // Load configuration files
        try {
            DLCConfigHelper dlcConfigHelper = DLCConfigHelper.getInstance();
            dlcConfigHelper.load();

            LoaderConfigHelper.getInstance().load(dlcConfigHelper.getDlcConfigMap());
        } catch (ConfigParsingException ex) {
            logger.error(ex.getMessage());
        } catch (Throwable th) {
            throw new Exception("Failed to load configuration files.", th);
        }

        // Initialize data source helper
        try {
            DataSourceHelper.getInstance().initialize(logger);
        } catch (ConfigParsingException ex) {
            logger.error(ex.getMessage());
        } catch (Throwable th) {
            throw new Exception("Failed to initialize data source helper.", th);
        }

        try {
            // Schedule and start the log configuration watching job
            SchedulableJob job = new LogConfigWatchingJob();
            job.schedule();
        } catch (Throwable th) {
            throw new Exception("Failed to schedule the log config watching job.", th);
        }

        try {
            // Schedule and start the HA heart beat job
            SchedulableJob job = new HeartbeatJob();
            job.schedule();
        } catch (Throwable th) {
            throw new Exception("Failed to schedule heart beat jobs.", th);
        }

        try {
            // Schedule and start the data loader job
            SchedulableJob job = new DataLoaderJob();
            job.schedule();
        } catch (Throwable th) {
            throw new Exception("Failed to schedule data loader jobs.", th);
        }

        int retries = 0;
        do {
            try {
                // Start DLC listener
                DLCListener.getInstance().startup();
                break;
            } catch (Throwable th) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw new Exception("Failed to start the master listener.", th);
                } else {
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (Throwable th1) {
                        // Suppress the error
                    }
                }
            }
        } while (true);

        try {
            // Schedule and start the housekeeping job
            SchedulableJob job = new HouseKeepingJob();
            job.schedule();
        } catch (Throwable th) {
            throw new Exception("Failed to schedule the housekeeping job.", th);
        }

        try {
            // Schedule and start the data recovery job
            SchedulableJob job = new DataRecoveryJob();
            job.schedule();
        } catch (Throwable th) {
            throw new Exception("Failed to schedule the data recovery job.", th);
        }
    }

    /**
     * Shutdown the ecosystem
     */
    public void shutdown() {
        DLCListener.getInstance().shutdown();
        SchedulableJob.shutdown();
        System.exit(EXIT_CODE_SHUTDOWN);
    }
}
