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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.clustertech.cloud.dlc.framework.writer.WritingResult;

/**
 * Provide utilities to manipulate object of the LoaderInstance
 */
public final class LoaderHelper {
    private static LoaderHelper helper = null;
    private final Map<String, LoaderInstance> loaderInstances = new HashMap<String, LoaderInstance>();
    private final Map<String, RecoveryInstance> recoveryInstances = new HashMap<String, RecoveryInstance>();

    /**
     * Preventing from creating instance outside
     */
    private LoaderHelper() {

    }

    /**
     * Get the instance of the class
     * @return
     */
    public synchronized final static LoaderHelper getInstance() {
        if (helper == null) {
            helper = new LoaderHelper();
        }

        return helper;
    }

    /**
     * Get a loader instance by name. If not matching instance found, create a new one
     * @param name
     * @return
     */
    public LoaderInstance getLoaderInstance(String name) {
        return this.getLoaderInstance(name, true);
    }

    /**
     * Get a loader instance by name
     * @param name
     * @return
     */
    public LoaderInstance getLoaderInstance(String name, boolean create) {
        LoaderInstance instance = this.loaderInstances.get(name);
        if (instance == null && create) {
            instance = new LoaderInstance(name);
            this.loaderInstances.put(name, instance);
        }

        return instance;
    }

    /**
     * Get a loader instance by name
     * @param name
     * @return
     */
    public RecoveryInstance getRecoveryInstance(String name, boolean create) {
        RecoveryInstance instance = this.recoveryInstances.get(name);
        if (instance == null && create) {
            instance = new RecoveryInstance(name);
            this.recoveryInstances.put(name, instance);
        }

        return instance;
    }

    /**
     * Remove a RecoveryInstance with the given name
     * @param name
     */
    public void removeRecoveryInstance(String name) {
        this.recoveryInstances.remove(name);
    }

    /**
     * Add a given loader instance
     * @param instance
     */
    public void addLoaderInstance(LoaderInstance instance) {
        this.loaderInstances.put(instance.getName(), instance);
    }

    /**
     * Add a given loader instance
     * @param instance
     */
    public void addRecoveryInstance(RecoveryInstance instance) {
        this.recoveryInstances.put(instance.getName(), instance);
    }

    /**
     * Add record written information in the delta into the result
     * @param resultMap
     * @param deltaMap
     * @return
     */
    public final static int calcRecWritten(Map<String, WritingResult> resultMap, Map<String, WritingResult> deltaMap) {
        for (Entry<String, WritingResult> entry : deltaMap.entrySet()) {
            WritingResult existingResult = resultMap.get(entry.getKey());
            if (existingResult == null) {
                existingResult = entry.getValue().clone();
            } else {
                existingResult.add(entry.getValue());
            }
            resultMap.put(entry.getKey(), existingResult);
        }

        int total = 0;
        for (WritingResult result : resultMap.values()) {
            total += result.getTotal();
        }

        return total;
    }
}
