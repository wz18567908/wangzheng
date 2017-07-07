/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.IConfigUpdateListener;

public final class LoggerHelper implements IConfigUpdateListener {
    private final static String LOG4J_PROPERTIES = "log4j.properties";
    private static String confFile = null;
    private static boolean cliLogger = false;
    private static LoggerHelper helper = null;
    private final static String LOGGER_PREFIX = "com.clustertech.cloud.dlc.";
    private final Properties loggerProp = new Properties();
    private final Properties tempProp = new Properties();
    private final Set<String> existingLoggers = new HashSet<String>();
    // the tmp log file is /tmp/ctreport.log
    private final static String TMP_LOG = File.separator + "tmp"
            + File.separator + "ctreport.log";

    /**
     * Get an instance of the LoggerHelper
     * @return
     */
    public static LoggerHelper getInstance() {
        if (helper != null) {
            return helper;
        }

        synchronized (LOG4J_PROPERTIES) {
            if (helper == null) {
                helper = new LoggerHelper();
            }
        }

        return helper;
    }

    /**
     * Construct the LoggerHelper
     */
    private LoggerHelper() {
        if (cliLogger == false) {
            configure();
        }
    }

    /**
     * @param type
     * @param clazz
     * @return
     */
    public final Logger getLogger(LoggerType type, String name) {
        return this.getLogger(type.name().toLowerCase() + "/" + name);
    }

    private final Logger getLogger(String name) {
        String loggerKey = LOGGER_PREFIX + name;
        if (loggerProp.contains(loggerKey) == false) {
            // The loader is not configured
            for (Entry<Object, Object> entry : tempProp.entrySet()) {
                String key = entry.getKey().toString();
                key = key.replaceAll("\\$\\{name\\}", name);
                String value = entry.getValue().toString();
                value = value.replaceAll("\\$\\{name\\}", name);
                loggerProp.put(key, value);
            }
            PropertyConfigurator.configure(loggerProp);
            this.existingLoggers.add(name);
        }
        return LoggerFactory.getLogger(loggerKey);
    }

    /**
     * @param type
     * @param clazz
     * @return
     */
    public final Logger getLogger(LoggerType type) {
        return this.getLogger(type.name().toLowerCase());
    }

    /**
     * If the log perperties got updated, reconfigure the logging system
     * @throws IOException
     * @see com.clustertech.cloud.dlc.framework.config.commons.IConfigUpdateListener#fireConfUpdate(com.clustertech.cloud.dlc.framework.config.commons.IConfigUpdateListener.ConfigType)
     */
    public boolean onConfigUpdated(ConfigType configType) throws IOException {
        if (configType == ConfigType.LOG_PROPERTIES) {
            configure();
            this.getLogger(LoggerType.DLC).info(
                    "Found modifies on the log4j configuration file. "
                            + "The logger has been reconfigured sucessfully.");
        }
        return true;
    }

    /**
     * Get last modified of the configuration file
     * @return
     */
    public final long getConfLastModified() {
        return new File(confFile).lastModified();
    }

    /**
     * @param temp
     *            true - configure for temporary logger handler
     */
    private synchronized final void configure() {
        loggerProp.clear();
        loggerProp.putAll(AppConfig.getInstance().getProperties());
        confFile = AppConfig.getInstance().getProperty(
                AppConfig.ConfigKey.CONF_DIR.name())
                + File.separator + LOG4J_PROPERTIES;

        FileReader reader = null;
        Properties prop = new Properties();
        try {
            reader = new FileReader(confFile);
            prop.load(reader);
            loggerProp.putAll(prop);
        }
        catch (FileNotFoundException e) {
            writeTmpLog(String.format("Initialization logger failed: "
                    + "the file %s does not exist", confFile));
            e.printStackTrace();
        }
        catch (IOException ex) {
            writeTmpLog(String.format("Initialization logger failed: "
                    + "There is IOException in loading file %s ", confFile));
            ex.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    writeTmpLog(String.format("There is IOException "
                            + "in closing FileReader for file %s ", confFile));
                }
            }
        }

        // Wash properties contains ${name}
        washDataLoaderProps();

        // Prepare properties for existing logger
        for (String name : this.existingLoggers) {
            String loggerKey = LOGGER_PREFIX + name;
            if (loggerProp.contains(loggerKey) == false) {
                // The loader is not configured
                for (Entry<Object, Object> entry : tempProp.entrySet()) {
                    String key = entry.getKey().toString();
                    key = key.replaceAll("\\$\\{name\\}", name);
                    String value = entry.getValue().toString();
                    value = value.replaceAll("\\$\\{name\\}", name);
                    loggerProp.put(key, value);
                }
            }
        }

        PropertyConfigurator.configure(loggerProp);
    }

    /**
     * Filter log4j properties from loggerProp according to the log4j.properties.
     */
    private final void washDataLoaderProps() {
        for (Entry<Object, Object> entry : ((Properties) loggerProp.clone()).entrySet()) {
            Object objKey = entry.getKey();
            if (objKey instanceof String == false) {
                continue;
            }

            String key = (String) objKey;
            if (key.startsWith("log4j.") && key.contains("${name}")) {
                tempProp.put(key, entry.getValue());
                loggerProp.remove(key);
            }
        }
    }

    /**
     * Write the exception information to /tmp/ctreport.log
     * before initialization logger
     */
    public void writeTmpLog(String exception) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(TMP_LOG, true);
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            writer.write(dateString + "  " + exception);
        }
        catch (IOException ex) {
            System.out.println("Fail to write file " + TMP_LOG);
            ex.printStackTrace();
        }
        finally {
            if ( writer != null ){
                try {
                        writer.close();
                }
                catch (IOException e) {
                    System.out.println("Fail to close file " + TMP_LOG);
                    e.printStackTrace();
                }
            }
        }
    }

    public enum LoggerType {
        DLC, DATALOADER, CLI, REPORT
    }

    /**
     * @return the confFile
     */
    public static final String getConfFile() {
        return confFile;
    }
}
