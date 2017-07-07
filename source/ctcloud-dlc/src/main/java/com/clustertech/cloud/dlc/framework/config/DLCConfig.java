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

/**
 * The DLCConfig class provide configuration information of data loader controller
 */
public class DLCConfig {
    /** Name of the data loader */
    private String name;
    /** Interval in seconds */
    private int interval = 0;
    /** Cron scheduling string */
    private String cron;
    /** The path of the data loader configuration file (relative path to DCN_TOP/conf/dataloaders */
    private String configPath;
    /** Flag indicates whether the loader is enabled */
    private boolean enabled = false;

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @return the interval
     */
    public final int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public final void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * @return the cron
     */
    public final String getCron() {
        return cron;
    }

    /**
     * @param cron the cron to set
     */
    public final void setCron(String cron) {
        this.cron = cron == null || "".equals(cron) ? null : cron;
    }

    /**
     * @return the dlConfigPath
     */
    public final String getConfigPath() {
        return configPath;
    }

    /**
     * @param dlConfigPath the dlConfigPath to set
     */
    public final void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * @return the enable
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enable the enable to set
     */
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("DLCConfig [name=%s, interval=%s, cron=%s, configPath=%s, enabled=%s]", name, interval,
                cron, configPath, enabled);
    }
}
