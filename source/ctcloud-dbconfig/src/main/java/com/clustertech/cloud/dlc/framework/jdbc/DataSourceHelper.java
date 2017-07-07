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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;

/**
 * The DataSurce Util class. This class only wrap how to get DataSource,
 * DataSourceType, Connection instance, etc.
 */
public class DataSourceHelper {

    /**
     * The private static instance of DatasourceUtil.
     */
    private static DataSourceHelper helper;

    /**
     * The static DataSource manager class. This class maintain the DataSource
     * instance that defined in configuration file.
     */
    private DataSourceManager dsManager;

    /**
     * The retry times of get connection.
     */
    private final int retry = 2;

    /**
     * The retry interval of get connection. The unit is millisecond.
     */
    private final long retryInterval = 3000;

    private static MessageHelper msgUtil = new MessageHelper("com/clustertech/cloud/dlc/framework/jdbc/Resources");

    /**
     * Private constructor.
     */
    private DataSourceHelper() {
    }

    /**
     * The synchonized method to get the instance of DatasourceUtil.
     * @return the identify instance of DatasourceUtil.
     * @throws ConfigParsingException
     * @throws Exception
     */
    public synchronized static DataSourceHelper getInstance() {
        if (helper == null) {

            // Init DataSourceManager
            helper = new DataSourceHelper();
        }
        return helper;
    }

    public void initialize(Logger logger) throws ConfigParsingException {
        // Init DataSourceManager instance with given conf file
        DataSourceManager manager = new DataSourceManager(logger);
        manager = manager.configure(AppConfig.getInstance().getProperty(ConfigKey.CONF_DIR));
        if (manager.getDataSources().size() <= 0) {
            throw new RuntimeException(msgUtil.getMessage("db.no.datasource.found"));
        }
        helper.setDsManager(manager);
    }

    /**
     * Get a Connection instance with the name of given DataSource. If can't
     * find the datasource instance return null.
     * @param dataSourceName
     *            the name of DataSource
     * @return An instance of Connection, if can't find return null.
     * @throws SQLException
     */
    public Connection getConnection(String dataSourceName) throws SQLException {
        Connection conn = null;
        DataSource dataSource = dsManager.getDataSource(dataSourceName);
        if (dataSource == null) {
            throw new IllegalArgumentException(String.format(
                    "The data source '{}' does not exist", dataSourceName));
        }

        for (int i = 0; i < retry; i++) {
            conn = dataSource.getConnection();
            if (conn != null) {
                break;
            }

            try {
                Thread.sleep(retryInterval);
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }
        return conn;
    }

    /**
     * Get the DataSource Type with given datasource name.
     * @param dataSourceName
     *            the name of DataSource
     * @return a DataSource Type.
     */
    public DataSourceType getDataSourceType(String dataSourceName) {
        DataSourceType type = dsManager.getDataSourceType(dataSourceName);
        if (type == null) {
            throw new IllegalArgumentException(String.format(
                    "The data source '%s' does not exist", dataSourceName));
        }
        return type;
    }

    /**
     * Get instance of DataSourceManager.
     * @return instance of DataSourceManager
     */
    public DataSourceManager getDsManager() {
        return dsManager;
    }

    /**
     * @param manager
     */
    public void setDsManager(DataSourceManager manager) {
        this.dsManager = manager;
    }

    /**
     * Check if database is available.
     * @return Database status. True -- available. False -- unavailable.
     */
    public boolean checkDbAvail(String dsName, Logger logger) {
        Connection conn = null;
        try {
            conn = this.getConnection(dsName);
            if (conn == null) {
                if (logger != null && logger.isDebugEnabled()) {
                    logger.debug("Failed to get database connection.");
                }
                return false;
            }

            // set auto commit to false
            conn.setAutoCommit(false);
            conn.commit();

        }
        catch (Exception ex) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug("Failed to get database connection.", ex);
            }
            return false;
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception e) {
                logger.warn("There is exception in close database connection.",
                        e);
            }
        }
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug("Got database connection successfully.");
        }
        return true;
    }
}
