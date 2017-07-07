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

import java.io.IOException;

/**
 * The IConfigUpdateListener listens on configuration updates
 */
public interface IConfigUpdateListener {
    /**
     * Config type
     */
    public enum ConfigType {
        LOG_PROPERTIES
    }

    public boolean onConfigUpdated(ConfigType configType) throws IOException;
}
