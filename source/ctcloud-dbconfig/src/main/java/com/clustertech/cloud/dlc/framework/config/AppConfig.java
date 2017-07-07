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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.NetworkingHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.IConfigUpdateListener.ConfigType;

public final class AppConfig {
    private static AppConfig appConfig = null;
    private final Properties prop = new Properties();
    private final List<IConfigUpdateListener> configUpdateListeners = Collections
            .synchronizedList(new ArrayList<IConfigUpdateListener>());

    /**
     * Get an instance of the AppConfig
     * @param thin
     *            - if true, load only service config, otherwise, load all
     *            configurations
     * @return
     */
    public final static AppConfig getInstance() {
        if (appConfig != null) {
            return appConfig;
        }

        synchronized (PATTERN_PROPERTY) {
            if (appConfig == null) {
                appConfig = new AppConfig();
                appConfig.configure();
            }
        }

        return appConfig;
    }

    /**
     * Broadcast configuration update to listeners
     * @param configType
     */
    public void fireConfigUpdate(ConfigType configType) {
        final Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
        String configFile = null;
        switch (configType) {
        case LOG_PROPERTIES:
            configFile = LoggerHelper.getConfFile();
            break;
        }

        int errors = 0;
        for (IConfigUpdateListener listener : this.configUpdateListeners) {
            boolean ret;
            try {
                ret = listener.onConfigUpdated(configType);
                if (ret == false) {
                    errors++;
                }
            }
            catch (IOException e) {
                errors++;
                logger.error("Failed to fire configuration update.", e);
            }
        }

        String message = errors == 0 ? "Reconfigured sucessfully" : "Reconfigured with " + errors + " error(s)";

        logger.info("Found update on configuration file: {}. {}.", new Object[] { configFile, message });
    }

    /**
     * Add a configuration update listener
     * @param listner
     */
    public boolean addConfigUpdateListener(IConfigUpdateListener listener) {
        return this.configUpdateListeners.add(listener);
    }

    /**
     * Remove a configuration update listener
     * @param listener
     */
    public boolean removeConfigUpdateListener(IConfigUpdateListener listener) {
        return this.configUpdateListeners.remove(listener);
    }

    /**
     * @param thin
     */
    private final void configure() {
        // Put all the system properties into the prop
        this.prop.putAll(System.getProperties());
        // Put all the environment into the prop
        this.prop.putAll(System.getenv());

        final String topDir = System.getProperty(ConfigKey.TOP_DIR.name());
        // If the CONF_DIR is not set, defaults to TOP_DIR/conf
        if (this.prop.containsKey(ConfigKey.CONF_DIR.name()) == false) {
            this.prop.put(ConfigKey.CONF_DIR.name(), topDir + File.separator + "conf");
        }

        // If the LOG_DIR is not set, defaults to TOP_DIR/logs
        if (this.prop.containsKey(ConfigKey.LOG_DIR.name()) == false) {
            this.prop.put(ConfigKey.LOG_DIR.name(), topDir + File.separator + "logs");
        }

        // If the ENV_DIR is not set, defaults to TOP_DIR/etc
        if (this.prop.containsKey(ConfigKey.ENV_DIR.name()) == false) {
            this.prop.put(ConfigKey.ENV_DIR.name(), topDir + File.separator + "etc");
        }

        // If the WORKING_DIR is not set, defaults to TOP_DIR/work
        if (this.prop.containsKey(ConfigKey.WORKING_DIR.name()) == false) {
            this.prop.put(ConfigKey.WORKING_DIR.name(), topDir + File.separator + "work");
        }

        // If the SHARE_DIR is not set, defaults to TOP_DIR/work/share
        if (this.prop.containsKey(ConfigKey.SHARE_DIR.name()) == false) {
            this.prop.put(ConfigKey.SHARE_DIR.name(), topDir + File.separator
                    + "work" + File.separator + "share");
        }

        // If the ETC_DIR is not set, defaults to TOP_DIR/etc
        if (this.prop.containsKey(ConfigKey.ETC_DIR.name()) == false) {
            this.prop.put(ConfigKey.ETC_DIR.name(), topDir + File.separator + "etc");
        }

        // Get host name
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            hostName = "localhost";
            LoggerHelper.getInstance().writeTmpLog(
                    "Cannot get hostname. Assume hostname to be localhost");
            e.printStackTrace();
        }
        this.prop.put(ConfigKey.HOST_NAME.name(), hostName);
    }

    public final void initialize() {
        Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

        // Wash port
        String strPort = this.prop.getProperty(ConfigKey.DLC_PORT.name());
        if (strPort != null) {
            Matcher matcher = PATTERN_NUM.matcher(strPort);
            if (matcher.find() == false) {
                logger.warn("The SERVICE_MASTER_PORT is invalid: {}. Defaulting to {}.", new Object[] { strPort,
                        NetworkingHelper.DEF_DLC_PORT });
                strPort = String.valueOf(NetworkingHelper.DEF_DLC_PORT);
            }
        }
        else {
            logger.warn("The SERVICE_MASTER_PORT is not specified: {}. Defaulting to {}.", new Object[] { strPort,
                    NetworkingHelper.DEF_DLC_PORT });
            strPort = String.valueOf(NetworkingHelper.DEF_DLC_PORT);
        }
        this.prop.put(ConfigKey.DLC_PORT.name(), strPort);
    }

    /**
     * @return
     */
    public final Properties getProperties() {
        return this.prop;
    }

    /**
     * Get property according to a given key
     * @param key
     * @return
     */
    public final String getProperty(String key) {
        return this.prop.getProperty(key);
    }

    /**
     * Get property according to a given key. If the key is not found, return
     * default value
     * @param key
     * @param defaultValue
     * @return
     */
    public final String getProperty(String key, String defaultValue) {
        return this.prop.getProperty(key, defaultValue);
    }

    /**
     * Get property according to a given key
     * @param key
     * @return
     */
    public final String getProperty(ConfigKey key) {
        return this.prop.getProperty(key.name());
    }

    /**
     * Get property according to a given key. If the key is not found, return
     * default value
     * @param key
     * @param defaultValue
     * @return
     */
    public final String getProperty(ConfigKey key, String defaultValue) {
        return this.prop.getProperty(key.name(), defaultValue);
    }

    // Pattern used for washing leading and followed " or ', wash only one pair
    private static final Pattern PATTERN_PROPERTY = Pattern.compile("^[\"'](.*)[\"']$");
    private static final Pattern PATTERN_NUM = Pattern.compile("^\\d+$");

    public static enum ConfigKey {
        // Top directory
        TOP_DIR,
        // Config dir for the system
        CONF_DIR,
        // Loging dir for the system
        LOG_DIR,
        // Working dir for the system
        WORKING_DIR,
        // Share dir for the system
        SHARE_DIR,
        // Host name
        HOST_NAME,
        // Etc dir for the system
        ETC_DIR,
        // Env dir for the system
        ENV_DIR,
        // Master host. when it is local hostName, local host is the primary master.
        DLC_MASTER_HOST,
        // Port number
        DLC_PORT
    }
}
