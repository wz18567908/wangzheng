/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.operation;

import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;

/**
 * Operation executor
 */
public interface IOperationExecutor {
    public Acknowledgment execute() throws OperationException;
}
