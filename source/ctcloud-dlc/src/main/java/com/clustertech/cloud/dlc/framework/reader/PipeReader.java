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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.clapper.util.text.MapVariableDereferencer;
import org.clapper.util.text.UnixShellVariableSubstituter;

import au.com.bytecode.opencsv.CSVParser;

import com.clustertech.cloud.dlc.framework.commons.Record;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.ReaderConfig;

/**
 * The PipeReader calls a give executable and reads stdout from pipe. Outputs of the executable
 * should be in CSV format.
 * The header of the CSV format is defined in the corresponding data loader configuration file.
 * If the header is not defined in the configuration file, the output should includes the header.
 */
public class PipeReader extends DataReader {
    // Static variables
    private final static String EXECUTABLE_KEY = "executable";
    private final static String TIMEOUT_KEY = "timeout";
    private final static String SKIP_HEADER_KEY = "skipHeader";
    private final static String DELIMITER_KEY = "delimiter";
    private final static String QUOTE_KEY = "quote";
    private final static String ESCAPE_KEY = "escape";
    private final static String TABLE_NAME_KEY = "__TABLE_NAME__";
    private final static String TIME_STAMP_KEY = "TIME_STAMP";
    private final static long DEF_TIMEOUT = 3600000;

    // Non-static variables
    private String executable = null;
    private long timeout = DEF_TIMEOUT; // Timeout in millisecond
    private boolean skipHeader = false;
    private Map<String, String[]> headers = new HashMap<String, String[]>();
    private BlockingQueue<Record> buffer = null;
    private boolean readingDone = false;
    private ExecuteException execException = null;
    private CSVParser csvParser = null;
    private int bufferSize = 0;
    ExecuteWatchdog watchdog = null;
    private boolean moreRecord = false;

    /**
     * Create an instance of PipeReader with given name and configuration
     * @param name name of the reader
     * @param config configuration of the reader
     */
    public PipeReader(String name, ReaderConfig config) {
        super(name, config);

        this.bufferSize = config.getMaxBufferSize();
        // Initialize the buffer
        this.buffer = new ArrayBlockingQueue<Record>(this.bufferSize);

        // Get executable from the configuration
        try {
            String executable = config.getProperties().get(EXECUTABLE_KEY);
            UnixShellVariableSubstituter varSubstituter = new UnixShellVariableSubstituter();
            this.executable = varSubstituter.substitute(executable,
                    new MapVariableDereferencer(AppConfig.getInstance()
                            .getProperties()));
            this.logger.debug("Executable: {}", this.executable);
        } catch (Throwable th) {
            throw new IllegalArgumentException(th);
        }

        // Get the timeout property from the configuration
        try {
            String timeout = config.getProperties().get(TIMEOUT_KEY);
            if (timeout != null && "".equals(timeout) == false) {
                this.timeout = Long.parseLong(timeout) * 1000;
            }
            this.logger.debug("Timeout: {}", this.timeout);
        } catch (Exception ex) {
            this.logger.warn("Invalid timeout parameter. Defaulting to {}.",
                    this.timeout);
        }

        // Get the skipHeader property from the configuration
        String skipHeader = config.getProperties().get(SKIP_HEADER_KEY);
        if ("true".equals(skipHeader)) {
            this.skipHeader = true;
        }
        this.logger.debug("Skip header: {}", this.skipHeader);

        // Get the delimiter from the configuration
        char delimiter = ',';
        String delimiterStr = config.getProperties().get(DELIMITER_KEY);
        if (delimiterStr != null && delimiterStr.length() > 0) {
            delimiter = delimiterStr.charAt(0);
        }
        this.logger.debug("Delimiter: {}", delimiter);

        // Get the quote character from the configuration
        char quote = '"';
        String quoteStr = config.getProperties().get(QUOTE_KEY);
        if (quoteStr != null && quoteStr.length() > 0) {
            quote = quoteStr.charAt(0);
        }
        this.logger.debug("Quote: {}", quote);

        // Get the escape character from the configuration
        char escape = '\\';
        String escapeStr = config.getProperties().get(ESCAPE_KEY);
        if (escapeStr != null && escapeStr.length() > 0) {
            escape = escapeStr.charAt(0);
        }
        this.logger.debug("Escape: {}", escape);

        // Get extra configuration
        this.headers = config.getHeaders();

        this.csvParser = new CSVParser(delimiter, quote, escape);
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#preRead()
     */
    @Override
    public void preRead() throws DataReadingException {
        CommandLine cmdLine = CommandLine.parse(this.executable + " "
                + (this.getStartTime() - this.getInterval()) / 1000 + " "
                + this.getInterval() / 1000);

        DefaultExecutor executor = new DefaultExecutor();
        // Set exit value for a successful execution
        executor.setExitValue(0);
        // for the data complete, the timeStamp - interval
        final Timestamp timeStamp = new Timestamp(this.getStartTime() - this.getInterval());

        final LogOutputStream stderrHandler = new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                logger.error(line);
            }
        };

        final LogOutputStream stdoutHandler = new LogOutputStream() {
            private int lineNo = 0;

            @Override
            protected void processLine(String line, int level) {
                if (logger.isTraceEnabled()) {
                    logger.trace(line);
                }

                this.lineNo++;
                if (skipHeader == true) {
                    return;
                }

                try {
                    String[] values = csvParser.parseLine(line);
                    if (values.length < 2) {
                        return;
                    }
                    // Get the header
                    String tableName = values[0];
                    String[] headerValues = headers.get(tableName);
                    if (headerValues == null) {
                        logger.warn("No header found for the table '{}'. Ignore the line {}.", tableName, lineNo);
                        return;
                    }
                    // Create a record
                    Record record = new Record();
                    // Set table name and timestamp for the record
                    record.put(TABLE_NAME_KEY, tableName);
                    record.put(TIME_STAMP_KEY, timeStamp);
                    int colNum = values.length > headerValues.length ? headerValues.length : values.length;
                    for (int idx = 1; idx < colNum; idx++) {
                        record.put(headerValues[idx], values[idx]);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("The record for line {} is: {}", lineNo, record);
                    }
                    // Put into the queue
                    buffer.offer(record);
                } catch (IOException ex) {
                    logger.error("Failed to parse the line {}.", line, ex);
                }
            }
        };

        // Set stream handler for the executor
        final PumpStreamHandler streamHandler = new PumpStreamHandler(stdoutHandler, stderrHandler);
        executor.setStreamHandler(streamHandler);
        streamHandler.start();

        this.readingDone = false;
        this.execException = null;
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler() {
            @Override
            public void onProcessComplete(int exitValue) {
                logger.debug("The process completed with exit code {}.", exitValue);
                stderrHandler.flush();
                stdoutHandler.flush();
                readingDone = true;
            }

            @Override
            public void onProcessFailed(ExecuteException ex) {
                execException = ex;
                logger.debug(ex.getMessage());
                stderrHandler.flush();
                stdoutHandler.flush();
                readingDone = true;
            }
        };

        // Watchdog the process.
        this.watchdog = new ExecuteWatchdog(this.timeout);
        executor.setWatchdog(this.watchdog);

        try {
            // Execute the command line
            executor.execute(cmdLine, AppConfig.getInstance().getProperties(), resultHandler);
        } catch (Throwable th) {
            throw new DataReadingException(String.format("Failed to start the executable: %s.", cmdLine), th);
        }

        this.moreRecord = true;
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#readData()
     */
    @Override
    public Map<String, RecordList> readData() throws DataReadingException {
        Map<String, RecordList> data = new HashMap<String, RecordList>();
        int totalRead = 0;

        while (true) {
            try {
                Record record = this.buffer.poll(1L, TimeUnit.SECONDS);
                if (record != null) {
                    String table = (String) record.remove(TABLE_NAME_KEY);
                    if (table == null) {
                        table = "";
                    }

                    RecordList recList = data.get(table);
                    if (recList == null) {
                        recList = new RecordList();
                        data.put(table, recList);
                    }
                    recList.add(record);
                    totalRead++;
                }

                if (this.readingDone && this.buffer.size() == 0) {
                    if (this.execException != null) {
                        throw new DataReadingException(this.execException);
                    }
                    this.moreRecord = false;
                    break;
                }

                if (totalRead >= this.bufferSize) {
                    break;
                }
            } catch (InterruptedException e) {
                // Ignore the InterrupedException
            }
        }
        return data;
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#postRead()
     */
    @Override
    public void postRead() {
        try {
            if (this.watchdog != null) {
                this.watchdog.destroyProcess();
            }
        } catch (Throwable th) {
            this.logger.warn(th.getMessage());
        }
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#hasMoreData()
     */
    @Override
    public boolean hasMoreData() throws DataReadingException {
        return this.moreRecord;
    }
}
