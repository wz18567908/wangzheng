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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Set;

/**
 * A sub class of SQLExceptionTransformer, it use SQLState of SQLException to
 * transformer SQLException to DataAccessException.
 */
public class SQLStateTransformer extends SQLExceptionTransformer {

    /**
     * Transformer SQLException to DataAccessException by the error code of
     * SQLException. If the current configuration does not configure this
     * SQLState , it will return a UnknowDataAccessException instance.
     * @param ex
     *            an instance of SQLException
     * @return an instance of DataAccessException
     */
    public DataAccessException transform(SQLException ex) {
        DataAccessException dae = null;
        String sqlState = null;

        sqlState = ex.getSQLState();
        if (sqlState == null) {
            ex = ex.getNextException();
            if (ex != null)
                sqlState = ex.getSQLState();
        }

        if (sqlState == null) {
            dae = new UnknownDataAccessException();
        }
        else {
            for (String key : mapProperties.keySet()) {
                Set<String> sqlStates = mapProperties.get(key);
                if (sqlStates != null && sqlStates.contains(sqlState)) {
                    Class<DataAccessException> clazz = mapClass.get(key);
                    try {
                        Constructor<DataAccessException> constructor = clazz.getConstructor(Throwable.class);
                        Object obj = constructor.newInstance(new Object[] { ex });
                        if (obj != null) {
                            dae = (DataAccessException) obj;
                        }
                    }
                    catch (Throwable th) {
                        // do nothing
                    }
                    break;
                }
            }
        }

        if (dae == null) {
            dae = new UnknownDataAccessException(ex);
        }
        return dae;
    }
}
