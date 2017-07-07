/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.jdbc.dstools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceConfig;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceManager;

public class DBConfigHelper {
    public final static String DEF_DS_NAME = "CloudDB";
    private static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.CLI);

    public enum Action {
        ADD, EDIT, REMOVE
    }

    public static void testInputs(String dataSourceName, String userID, String password, String driver, String url,
            String driverPath) throws Exception {
        Connection conn = null;
        Class.forName(driver);
        conn = DriverManager.getConnection(url, userID, password);
        if (conn != null) {
            try {
                conn.close();
                }
            catch (Exception ex) {
                logger.error("There is exception when close the db connection.", ex);
                }
        }
    }

    public static void editDataSource(String dsName, String userID, String password, String driver, String url,
            String maxActive, boolean isDefault) throws IOException, ConfigParsingException {
        String confDir = AppConfig.getInstance().getProperty(ConfigKey.CONF_DIR);
        File newFile = new File(confDir + File.separator + DataSourceManager.DS_CONF_FILE);
        File oldFile = new File(confDir + File.separator + DataSourceManager.DS_CONF_FILE + ".old");
        FileUtils.copyFile(newFile, oldFile);
        logger.debug("Backup " + newFile.getAbsolutePath() + " to " + oldFile.getAbsolutePath());
        Properties propertyInfo = new Properties();
        propertyInfo.put("maxActive", maxActive);
        DataSourceConfig dsConfig = new DataSourceConfig();
        dsConfig.setName(dsName);
        dsConfig.setDriver(driver);
        dsConfig.setUrl(url);
        dsConfig.setUserName(userID);
        dsConfig.setPassword(password);
        dsConfig.setDefault(isDefault);

        dsConfig.addProperties(propertyInfo);

        DataSourceManager dsManager = DataSourceHelper.getInstance().getDsManager();
        dsManager.addDataSourceConfig(dsConfig);

        try {
            dsManager.writeConfig(AppConfig.getInstance().getProperty(ConfigKey.CONF_DIR));
        }
        catch (Exception ex) {
            throw new ConfigParsingException(newFile.getPath(), ex);
        }
    }
}
