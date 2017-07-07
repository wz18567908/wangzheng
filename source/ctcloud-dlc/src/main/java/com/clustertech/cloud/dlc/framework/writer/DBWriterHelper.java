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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.Record;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.SQLConfig;
import com.clustertech.cloud.dlc.framework.config.SQLParam;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;
import com.clustertech.cloud.dlc.framework.config.WriterConfig.ErrorHandler;
import com.clustertech.cloud.dlc.framework.jdbc.DataAccessException;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceType;
import com.clustertech.cloud.dlc.framework.jdbc.IgnorableDataAccessException;
import com.clustertech.cloud.dlc.framework.jdbc.SQLExceptionTransformer;
import com.clustertech.cloud.dlc.framework.jdbc.SQLExceptionTransformerFactory;

/**
 * Utilities for writing data to DB
 */
public final class DBWriterHelper {
    public final static String backupTop = AppConfig.getInstance().getProperty(
            ConfigKey.SHARE_DIR)
            + File.separator + "data-backup" + File.separator;
    public final static String backupFileSuffix = ".dat";

    private String preErrMsg = null;
    private int errRepeatedNum = 0;
    private Logger logger = null;

    public DBWriterHelper(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set parameter for the give PreparedStatement
     * @param stat
     * @param record
     * @param sqlConfig
     * @throws SQLException
     */
    public final void setSQLParams(PreparedStatement stat, Record record,
            SQLConfig sqlConfig) throws SQLException {
        // For each parameter
        int paramIdx = 1;
        for (SQLParam param : sqlConfig.getParamList()) {
            String name = param.getValue();
            Object objValue = record.get(name);
            Object value = null;

            // Value is null or value is '' and value is of string type
            if (objValue == null
                    || (objValue instanceof String && ((String) objValue)
                            .isEmpty())) {
                switch (param.getType()) {
                case STRING:
                    stat.setNull(paramIdx++, Types.VARCHAR);
                    break;

                case NUMERIC:
                    stat.setNull(paramIdx++, Types.NUMERIC);
                    break;

                case DOUBLE:
                    stat.setNull(paramIdx++, Types.DOUBLE);
                    break;

                case INTEGER:
                    stat.setNull(paramIdx++, Types.INTEGER);
                    break;

                case TIMESTAMP:
                    stat.setNull(paramIdx++, Types.TIMESTAMP);
                    break;

                case TIME:
                    stat.setNull(paramIdx++, Types.TIME);
                    break;

                case DATE:
                    stat.setNull(paramIdx++, Types.DATE);
                    break;

                default:
                    stat.setNull(paramIdx++, Types.VARCHAR);
                    break;

                } // End for switch (param.getType())
                continue;
            } // End for if (objValue == null)
            else if (objValue instanceof String) {
                String strValue = (String) objValue;
                strValue = strValue.trim();
                switch (param.getType()) {
                case STRING:
                    value = objValue;
                    break;

                case INTEGER:
                    try {
                        value = Long.parseLong(strValue);
                    } catch (Throwable th) {
                        value = 0L;
                        this.logger.debug("Failed to parse the given integer value '{}'. Defaulting to 0.", strValue);
                    }
                    break;

                case NUMERIC:
                    try {
                        value = new BigDecimal(strValue);
                    } catch (Throwable th) {
                        value = 0.0D;
                        this.logger.debug("Failed to parse the given numeric value '{}'. Defaulting to 0.", strValue);
                    }
                    break;

                case DOUBLE:
                    try {
                        value = Double.parseDouble(strValue);
                    } catch (Throwable th) {
                        value = 0.0D;
                        this.logger.debug("Failed to parse the given double value '{}'. Defaulting to 0.", strValue);
                    }
                    break;

                case TIMESTAMP:
                    try {
                        value = strValue.length() > 20 ? new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss.SSS").parse(strValue)
                                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        .parse(strValue);
                    } catch (Exception ex) {
                        this.logger.error(
                                "Failed to parse the given timestamp '{}'. Defaulting to '1970-01-01 00:00:00'.",
                                strValue, ex);
                    }
                    break;

                case DATE:
                    try {
                        value = new SimpleDateFormat("yyyy-MM-dd")
                                .parseObject(strValue);
                    } catch (Exception ex) {
                        this.logger.error("Failed to parse the given date '{}'. Defaulting to '1970-01-01'.", strValue,
                                ex);
                    }
                    break;

                case TIME:
                    try {
                        value = new SimpleDateFormat("HH:mm:ss")
                                .parseObject(strValue);
                    } catch (Exception ex) {
                        this.logger
                                .error("Failed to parse the given time '{}'. Defaulting to '00:00:00'", strValue, ex);
                    }
                default:
                    value = null;
                    break;
                }
            } // End for else if (objValue instanceof String)
            else {
                value = objValue;
            } // End of else
            stat.setObject(paramIdx++, value);

        } // End for for (SQLParam param : sqlConfig.g etParamList())
    }

    /**
     * Write records to disk
     * @param name
     * @param recList
     * @param sqlConfig
     * @param logger
     * @throws DataWritingException
     */
    public final void writeRecordsToDisk(String name, RecordList recList, SQLConfig sqlConfig)
            throws DataWritingException {
        String backupDir = backupTop + name;
        File dir = new File(backupDir);
        if (dir.exists() == false) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException ex) {
                throw new DataWritingException(ex);
            }
        }

        // Get working file name
        File workingFile = null;
        long timestamp = 0;
        // Make sure no existing file will be overwritten
        while (true) {
            timestamp = System.currentTimeMillis();
            workingFile = new File(backupDir + File.separator + String.valueOf(timestamp));
            if (workingFile.exists() == false) {
                break;
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // Ignore the error
            }
        }

        PersistedRecords persistedRecords = new PersistedRecords();
        persistedRecords.setRecList(recList);
        persistedRecords.setSqlConfigs(sqlConfig);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(workingFile);
            ObjectOutputStream output = new ObjectOutputStream(fileOutputStream);
            output.writeObject(persistedRecords);

            // Move the working file to a file with name followed by '.dat'

            File finalFile = null;
            // Make sure no existing file will be overwritten
            while (true) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(backupDir).append(File.separator).append(timestamp).append(".")
                        .append(sqlConfig.getTable());
                if (sqlConfig.getDs() != null) {
                    buffer.append(".").append(sqlConfig.getDs());
                }
                buffer.append(backupFileSuffix);

                finalFile = new File(buffer.toString());
                if (finalFile.exists() == false) {
                    break;
                }

                timestamp++;
            }

            // Close if file descriptor before move the file
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (IOException e) {
                //Ignore the error
            }
            FileUtils.moveFile(workingFile, finalFile);
        } catch (Throwable th) {
            throw new DataWritingException(th);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (IOException e) {
                //Ignore the error
            }
        }
    }

    /**
     * Write records got from loader
     * @param name
     * @param conn
     * @param stat
     * @param recList
     * @param sqlConfig
     * @param errorTransformer
     * @return
     * @throws DataWritingException
     */
    public final WritingResult writeRecordsFromLoader(String name, Connection conn, PreparedStatement stat,
            RecordList recList, SQLConfig sqlConfig, SQLExceptionTransformer errorTransformer, String errorHandler)
            throws DataWritingException {

        int startIndex = 0;
        int endIndex = 0;
        final RecordList recordToPersisted = new RecordList();
        int numWrittenToTable = 0;
        int numWrittenToDisk = 0;
        int numIgnored = 0;
        boolean isDBAvail = conn != null && stat != null;
        /*PreparedStatement ps = null;
        boolean psFlag = false;*/

        // For each record
        for (Record record : recList) {
            endIndex++;
            try {
                if (isDBAvail == true) {
                    this.setSQLParams(stat, record, sqlConfig);
                    stat.addBatch();
                } else {
                    // Failed to get the connection or statement in the preWrite
                    // Add records into the list where records will be persisted to disk
                    if (errorHandler.equals(ErrorHandler.DLP.toString())) {
                        recordToPersisted.add(record);
                    } else if (errorHandler.equals(ErrorHandler.EXIT.toString())) {
                        throw new DataWritingException(
                                "The db connection is not available.");
                    }
                }

                if ((endIndex - startIndex) >= sqlConfig.getMaxBatchSize()
                        || endIndex == recList.size()) {
                    // Execute the batch and commit the transaction
                    if (isDBAvail == true) {
                        stat.executeBatch();
                        conn.commit();
                    } else {
                        // If the error handler is DLP, write record onto disk
                        if (errorHandler.equals(ErrorHandler.DLP.toString())) {
                            this.writeRecordsToDisk(name, recordToPersisted, sqlConfig);
                            numWrittenToDisk += recordToPersisted.size();
                            recordToPersisted.clear();
                        } else if (errorHandler.equals(ErrorHandler.EXIT.toString())) {
                            throw new DataWritingException("The db connection is not available.");
                        }
                    }
                    startIndex = endIndex;
                }
            } catch (SQLException ex) {
                try {
                    stat.clearBatch();
                    conn.rollback();
                } catch (SQLException ex1) {
                    logger.error("There is exception when rollback db:" + ex1);
                }

                DataAccessException exception = errorTransformer.transform(ex);
                if (exception instanceof IgnorableDataAccessException == true) {
                    // Ignorable exception. Insert record one by one
                    int tmpIdx = 0;
                    for (Record tmpRecord : recList) {
                        tmpIdx++;
                        // Seek the start index of the record to be persisted to disk
                        if (tmpIdx < startIndex) {
                            continue;
                        }

                        // If the current index is the endIndex break
                        if (tmpIdx > endIndex) {
                            break;
                        }

                        try {
                            this.setSQLParams(stat, tmpRecord, sqlConfig);
                            stat.execute();
                            conn.commit();
                        } catch (SQLException ex1) {
                            DataAccessException exception1 = errorTransformer
                                    .transform(ex);
                            if (exception1 instanceof IgnorableDataAccessException == false
                                    && errorHandler.equals(ErrorHandler.DLP
                                            .toString())) {
                                recordToPersisted.add(tmpRecord);
                            } else {
                                // Ignore the record
                                numIgnored++;
                                this.printSQLException(ex1, true);
                            }
                        }
                    }
                } else {
                    this.printSQLException(ex, false);
                    // Non-ignorable exception and error handler is DLP. Persisted data onto disk
                    if (errorHandler.equals(ErrorHandler.DLP.toString())) {
                        int tmpIdx = 0;
                        for (Record tmpRecord : recList) {
                            tmpIdx++;
                            // Seek the start index of the record to be persisted to disk
                            if (tmpIdx < startIndex) {
                                continue;
                            }
                            // If the current index is the endIndex break
                            if (tmpIdx > endIndex) {
                                break;
                            }
                            recordToPersisted.add(tmpRecord);
                        }
                    }
                }
                startIndex = endIndex;

                if (recordToPersisted.size() > 0) {
                    this.writeRecordsToDisk(name, recordToPersisted, sqlConfig);
                    numWrittenToDisk += recordToPersisted.size();
                    recordToPersisted.clear();
                    }
            }
        } // End for for (Record record : recList)

        numWrittenToTable = recList.size() - numWrittenToDisk - numIgnored;
        return new WritingResult(numWrittenToTable, numWrittenToDisk, numIgnored);
    }

    /**
     * Write the given PersistedRecords into database
     * @param conn
     * @param persistedRecords
     * @param logger
     * @return
     * @throws DataWritingException
     */
    public final WritingResult writeRecordsFromDLP(RecordList recList, SQLConfig sqlConfig) throws DataWritingException {

        SQLExceptionTransformer errorTransformer = null;
        Connection conn = null;
        PreparedStatement stat = null;

        try {
            // Get SQLExceptionTransformer
            SQLExceptionTransformerFactory transFactory = SQLExceptionTransformerFactory.getInstance();
            DataSourceType dsType = DataSourceHelper.getInstance().getDataSourceType(sqlConfig.getDs());
            errorTransformer = transFactory.getTransformer(dsType.name());

            // Get PreparedStatement
            conn = DataSourceHelper.getInstance().getConnection(sqlConfig.getDs());
            conn.setAutoCommit(false);
            stat = conn.prepareStatement(sqlConfig.getStatement());

        } catch (Throwable th) {
            throw new DataWritingException(th);
        }

        int numWrittenToTable = 0;
        int numIgnored = 0;

        try {
            // For each record
            for (Record record : recList) {
                this.setSQLParams(stat, record, sqlConfig);
                stat.addBatch();
            } // End for for (Record record : recList)
            stat.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            try {
                stat.clearBatch();
                conn.rollback();
            } catch (SQLException ex1) {
                //Ignore the error
            }

            DataAccessException exception = errorTransformer.transform(ex);
            if (exception instanceof IgnorableDataAccessException == true) {
                // Ignorable exception. Insert record one by one
                for (Record tmpRecord : recList) {
                    try {
                        this.setSQLParams(stat, tmpRecord, sqlConfig);
                        stat.execute();
                    } catch (SQLException ex1) {
                        DataAccessException exception1 = errorTransformer.transform(ex);
                        if (exception1 instanceof IgnorableDataAccessException == false) {
                            throw new DataWritingException(ex);
                        } else {
                            // Ignore the record
                            numIgnored++;
                            this.printSQLException(ex1, true);
                        }
                    }
                } // End of for (Record tmpRecord : recList)
                    // Commit the transaction
                try {
                    conn.commit();
                } catch (SQLException ex1) {
                    throw new DataWritingException(ex);
                }
            } // End of if (exception instanceof IgnorableDataAccessException == true)
            else {
                throw new DataWritingException(ex);
            }
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                // Ignore the error
            }

            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore the error
            }
        }

        numWrittenToTable = recList.size() - numIgnored;
        return new WritingResult(numWrittenToTable, 0, numIgnored);
    }

    /**
     * Print SQLException without repeat
     * @param ex
     * @param ignorable
     */
    public void printSQLException(SQLException ex, boolean ignorable) {
        if (ex instanceof BatchUpdateException && ex.getNextException() != null) {
            ex = ex.getNextException();
        }

        String errMsg = ex.getMessage();
        if (errMsg.equals(this.preErrMsg) == false) {
            if (this.errRepeatedNum > 1) {
                this.logger.error("The last error has repeated {} times.", this.errRepeatedNum);
            }
            this.errRepeatedNum = 0;

            this.preErrMsg = errMsg;
            if (ignorable == true) {
                logger.error("Failed to write record(s) into database due to error below. The record was skipped.", ex);
            } else {
                logger.error(
                        "Failed to write record(s) into database due to error below. Trying to persist data onto disk ...",
                        ex);
            }
        }
        this.errRepeatedNum++;
    }

    /**
     * Print repeated number of the last error
     */
    public void printRepeatedNum() {
        // Clean up the error message
        if (this.errRepeatedNum > 1) {
            this.logger.error("The last error has repeated {} times.", this.errRepeatedNum);
        }

        this.errRepeatedNum = 0;
        this.preErrMsg = null;
    }

    /**
     * Clean up the error message and repeated number of the last error
     */
    public void cleanError() {
        this.errRepeatedNum = 0;
        this.preErrMsg = null;
    }

}
