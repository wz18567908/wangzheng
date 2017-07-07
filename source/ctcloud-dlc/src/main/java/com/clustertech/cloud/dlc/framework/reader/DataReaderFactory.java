/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.reader;

import java.lang.reflect.Constructor;

import com.clustertech.cloud.dlc.framework.LoaderInitializationException;
import com.clustertech.cloud.dlc.framework.config.ReaderConfig;

/**
 * A factory to create DataReader instance
 */
public final class DataReaderFactory {
    public final static DataReader createDataReader(String name, ReaderConfig config)
            throws LoaderInitializationException {
        try {
            DataReader dataReader = null;
            Class<? extends DataReader> dataReaderClass = Class
                    .forName(config.getReaderClass()).asSubclass(DataReader.class);
            Constructor<? extends DataReader> dataReaderConstructor = dataReaderClass
                    .getConstructor(String.class, ReaderConfig.class);
            dataReader = dataReaderConstructor.newInstance(name, config);
            return dataReader;
        } catch (Throwable th) {
            throw new LoaderInitializationException(th);
        }
    }
}
