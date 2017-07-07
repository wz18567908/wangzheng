/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for transformer. It includes the relation between name and
 * Transformer class, name and ErrorCode or SQLState. Also, it defined an
 * abstract method to transformer a SQLException to a DataAccessException.
 */
public abstract class SQLExceptionTransformer {

    /**
     * this variable keeps the transform exception class for every DB.
     * the key is DB type, example MYSQL.
     */
    protected Map<String, Class<DataAccessException>> mapClass;
    /**
     * this variable keeps the exception for every DB. the key is DB type,
     * example MYSQL.
     */
    protected Map<String, Set<String>> mapProperties;

    /**
     * transformer a SQLException to a DataAccessException.
     * @param ex
     *            an instance of SQLException
     * @return an instance of DataAccessException
     */
    public abstract DataAccessException transform(SQLException ex);

    public Map<String, Class<DataAccessException>> getMapClass() {
        return mapClass;
    }

    public void setMapClass(Map<String, Class<DataAccessException>> mapClass) {
        this.mapClass = mapClass;
    }

    public Map<String, Set<String>> getMapProperties() {
        return mapProperties;
    }

    public void setMapProperties(Map<String, Set<String>> mapProperties) {
        this.mapProperties = mapProperties;
    }
}
