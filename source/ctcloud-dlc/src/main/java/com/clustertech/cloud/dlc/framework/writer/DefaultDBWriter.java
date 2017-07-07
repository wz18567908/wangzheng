/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.SQLConfig;
import com.clustertech.cloud.dlc.framework.config.WriterConfig;
import com.clustertech.cloud.dlc.framework.config.WriterConfig.ErrorHandler;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceType;
import com.clustertech.cloud.dlc.framework.jdbc.SQLExceptionTransformer;
import com.clustertech.cloud.dlc.framework.jdbc.SQLExceptionTransformerFactory;

/**
 * Default implementation of DataWriter class
 */
public class DefaultDBWriter extends DataWriter {
    private final Map<String, List<Connection>> connectionMap = new HashMap<String, List<Connection>>();
    private final Map<String, List<PreparedStatement>> statementMap = new HashMap<String, List<PreparedStatement>>();
    private final Map<String, SQLExceptionTransformer> errorTransformerMap = new HashMap<String, SQLExceptionTransformer>();

    private DBWriterHelper dbWriterHelper = null;

    /**
     * Create an instance with given name, configuration and logging handler
     * @param name
     * @param config
     * @param logger
     */
    public DefaultDBWriter(String name, WriterConfig config) {
        super(name, config);

        this.dbWriterHelper = new DBWriterHelper(this.logger);
    }

    /**
     * @see com.clustertech.report.dlc.framework.loader.DataWriter#preWrite()
     */
    @Override
    public void preWrite() throws DataWritingException {
        this.dbWriterHelper.cleanError();

        SQLExceptionTransformerFactory transFactory;
        try {
            transFactory = SQLExceptionTransformerFactory.getInstance();
        } catch (ConfigParsingException ex) {
            throw new DataWritingException(ex);
        }

        // Loop all SQLConfig object, initialize database connection
        for (List<SQLConfig> configList : this.config.getSqlConfigMap().values()) {
            for (SQLConfig sqlConfig : configList) {
                String dsName = sqlConfig.getDs();
                Connection conn = null;
                PreparedStatement stat = null;

                try {
                    if (!this.errorTransformerMap.containsKey(dsName)) {
                        DataSourceType dsType = DataSourceHelper.getInstance().getDataSourceType(dsName);
                        SQLExceptionTransformer transformer = transFactory.getTransformer(dsType.name());
                        this.errorTransformerMap.put(dsType.name(), transformer);
                    }

                    // Get database connection with the given data source name
                    conn = DataSourceHelper.getInstance().getConnection(dsName);
                    conn.setAutoCommit(false);

                    // Create a PreparedStatement with given connection and SQL statement
                    stat = conn.prepareStatement(sqlConfig.getStatement());
                } catch (Throwable th) {
                    this.logger.error("Failed to get database connection for the data source '{}'.", dsName, th);
                    if (this.config.getErrorHandler() == ErrorHandler.EXIT) {
                        // Error handler is exit. If database is not available, do not continue
                        throw new DataWritingException(th);
                    }
                }

                // Add the new connection to the connectionMap
                List<Connection> connList = this.connectionMap.get(sqlConfig.getTable());
                if (connList == null) {
                    connList = new ArrayList<Connection>();
                    this.connectionMap.put(sqlConfig.getTable(), connList);
                }
                connList.add(conn);

                // Add the new statement to the statementMap
                List<PreparedStatement> statList = this.statementMap.get(sqlConfig.getTable());
                if (statList == null) {
                    statList = new ArrayList<PreparedStatement>();
                    this.statementMap.put(sqlConfig.getTable(), statList);
                }
                statList.add(stat);
            }
        }
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.writer.DataWriter#writeData(java.util.List)
     */
    @Override
    public Map<String, WritingResult> writeData(Map<String, RecordList> data) throws DataWritingException {
        Map<String, WritingResult> resultMap = new HashMap<String, WritingResult>();

        // Loop all tables
        for (Entry<String, RecordList> entry : data.entrySet()) {
            Map<String, WritingResult> tmpMap = this.writeRecords(entry.getKey(), entry.getValue());
            resultMap.putAll(tmpMap);
        }
        return resultMap;
    }

    /**
     * Write records of into a single table
     * @param recList
     * @throws DataWritingException
     */
    private Map<String, WritingResult> writeRecords(String table, RecordList recList) throws DataWritingException {
        List<SQLConfig> sqlConfigList = this.config.getSqlConfig(table);
        Map<String, WritingResult> resultMap = new HashMap<String, WritingResult>();

        for (int dsIdx = 0; dsIdx < sqlConfigList.size(); dsIdx++) {
            Connection conn = this.connectionMap.get(table).get(dsIdx);
            PreparedStatement stat = this.statementMap.get(table).get(dsIdx);
            SQLConfig sqlConfig = this.config.getSqlConfig(table).get(dsIdx);

            DataSourceType dsType = DataSourceHelper.getInstance().getDataSourceType(sqlConfig.getDs());
            SQLExceptionTransformer errorTransformer = this.errorTransformerMap.get(dsType.name());

            if (errorTransformer == null) {
                throw new DataWritingException(String.format(
                        "SQLException transformer for the data source '%s' could not be found", dsType.name()));
            }
            String errorHandler = this.config.getErrorHandler().toString();
            WritingResult result = this.dbWriterHelper.writeRecordsFromLoader(this.name, conn, stat, recList,
                    sqlConfig, errorTransformer, errorHandler);
            resultMap.put(String.format("%s@%s", table, sqlConfigList.get(dsIdx).getDs()), result);
        }

        return resultMap;
    }

    /**
     * @see com.clustertech.report.dlc.framework.loader.DataWriter#postWrite()
     */
    @Override
    public void postWrite() {
        // Close all statement and clean up the statement map
        for (List<PreparedStatement> list : this.statementMap.values()) {
            for (Statement stat : list) {
                try {
                    if (stat != null) {
                        stat.close();
                    }
                } catch (Throwable th) {
                    // Ignore the error
                }
            }
        }
        this.statementMap.clear();

        // Close all connection and clean up the connection map
        for (List<Connection> list : this.connectionMap.values()) {
            for (Connection conn : list) {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Throwable th) {
                    // Ignore the error
                }
            }
        }
        this.connectionMap.clear();
        this.dbWriterHelper.printRepeatedNum();
    }
}
