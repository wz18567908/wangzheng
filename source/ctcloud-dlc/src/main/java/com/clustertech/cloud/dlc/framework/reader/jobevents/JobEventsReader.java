/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
 */
package com.clustertech.cloud.dlc.framework.reader.jobevents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.exec.ExecuteWatchdog;

import au.com.bytecode.opencsv.CSVParser;

import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.commons.Record;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.ReaderConfig;
import com.clustertech.cloud.dlc.framework.reader.AsciiFileReader;
import com.clustertech.cloud.dlc.framework.reader.DataReader;
import com.clustertech.cloud.dlc.framework.reader.DataReadingException;

/**
 *  This reader read the Torque's accounting file and load the job events(S and E) to DB.
 *  The accounting file location in Linux is: /var/spool/torque/server_priv/accounting
 *  The format is CSV, we need parse the line to record.
 */
public class JobEventsReader extends DataReader {
    // Static variables
    private final static String TIMEOUT_KEY = "timeout";
    private final static String SKIP_HEADER_KEY = "skipHeader";
    private final static String DELIMITER_KEY = "delimiter";
    private final static String QUOTE_KEY = "quote";
    private final static String ESCAPE_KEY = "escape";
    private final static long DEF_TIMEOUT = 3600000;
    private final static String TABLE_NAME_KEY = "__TABLE_NAME__";

    // Non-static variables
    // Timeout in millisecond
    private long timeout = DEF_TIMEOUT;
    private boolean skipHeader = false;
    private Map<String, String[]> headers = new HashMap<String, String[]>();
    private CSVParser csvParser = null;
    ExecuteWatchdog watchdog = null;
    private String tableName = null;

    /**
     * The temp file object, it storage the runtime information of
     * dataloader.Eg. file name, first line and last position.
     */
    protected File tempFile = null;

    private String filePath = null;
    // backup steam file location
    private String archivePath;
    private AsciiFileReader afReader = null;

    private final String EVENT_TYPE = "EVENT_TYPE";
    private String filterEvent;
    /**
     * filter string to avoid null string
     */
    private String filterStr = String.valueOf('\0');

    /**
     * job accounting control file name
     */
    protected final String JOB_ACCOUNTING = "accounting";

    /**
     * property file name
     */
    private String propertyPath;
    private final static String JOBEVENTS_PROPERTY = "jobevents.properties";
    private final String AUTO_DELETE_STREAM_FILE = "AUTO_DELETE_STREAM_FILE";
    private boolean isAutoDelFile = false;
    private final String ARCHIVE_PATH = "ARCHIVE_PATH";
    private boolean hasError = false;

    private String DEF_FILE_PATH = File.separator + "var" + File.separator
            + "spool" + File.separator + "torque" + File.separator
            + "server_priv" + File.separator + "accounting";

    private static MessageHelper msgUtil = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/Resources");

    public JobEventsReader(String name, ReaderConfig config) {
        super(name, config);

        // Get the timeout property from the configuration
        try {
            String timeout = config.getProperties().get(TIMEOUT_KEY);
            if (timeout != null && !"".equals(timeout)) {
                this.timeout = Long.parseLong(timeout) * 1000;
            }
            this.logger.debug("Timeout: {}", this.timeout);
        }
        catch (Exception ex) {
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
        Iterator<String> it = headers.keySet().iterator();
        while (it.hasNext()) {
            tableName = it.next();
            tableName = tableName == null ? "" : tableName.trim();
            if (tableName == null || "".equals(tableName)) {
                this.logger
                        .warn("Table is not specified for the header definition. Ignore the definition.");
                continue;
            }
        }
        this.csvParser = new CSVParser(delimiter, quote, escape);
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#preRead()
     */
    @Override
    public void preRead() throws DataReadingException {
        initEnv();
        initJobAcctProperty();
        if (afReader == null){
            afReader = new AsciiFileReader(logger, JOB_ACCOUNTING, filePath);
        }
        afReader.setAutoDelFile(isAutoDelFile);
        afReader.setArchivePath(archivePath);
        afReader.setFilePath(filePath);
        afReader.init();
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#readData()
     */
    @Override
    public Map<String, RecordList> readData() throws DataReadingException {
        Map<String, RecordList> data = new HashMap<String, RecordList>();
        int totalRead = 0;
        try {
            // Get record from job accounting file
            long lStart = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("Start reading records.");
            }
            List<String> vec = null;
            vec = afReader.read();
            // record is null, so no need to parse the record;
            if (vec == null || vec.size() == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No records.");
                }
                return null;
            }
            // save all not null records into list
            for (int i = 0; i < vec.size(); i++) {
                // Create a record
                String line = null;
                if (null == vec.get(i)) {
                    continue;
                } else {
                    line = vec.get(i);
                }
                if (line.indexOf(filterStr) > -1) {
                    logger.warn(msgUtil.getMessage(
                            "ct.report.dlc.dataloader.filterEscapeChar", line));
                    line = cleanEvent(line);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cleaned record: " + line);
                    }
                }

                if (line.contains("start=0")) {
                    //This is an invalid record generated by Torque; ignore.
                    logger.warn(msgUtil.getMessage(
                            "ct.report.dlc.dataloader.filterStart0", line));
                    continue;
                }

                if (checkEventType(line)) {
                    Record record = new Record();
                    record = processReocrd(line);
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
                }
            }
            logger.debug(totalRead
                    + " records have been read; Time to read: "
                    + (System.currentTimeMillis() - lStart) + " ms");
        } catch (Exception e) {
            throw new DataReadingException(e);
        }
        return data;
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#onDataWritten()
     */
    @Override
    public void onDataWritten() {
        afReader.updateControlFile();
    }

    private void initEnv() throws DataReadingException {
        if (filePath != null) {
            return;
        }
        if (filePath == null) {
            filePath = DEF_FILE_PATH;
        }
    }

    private void initJobAcctProperty() throws DataReadingException {
        propertyPath = AppConfig.getInstance().getProperty(
                AppConfig.ConfigKey.CONF_DIR) + File.separator + "dataloaders"
                + File.separator + JOBEVENTS_PROPERTY;
        FileInputStream in = null;
        Properties p = new Properties();
        try {
            in = new FileInputStream(propertyPath);
            p.load(in);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(
                    msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                            JOBEVENTS_PROPERTY));
        } catch (Exception ex) {
            throw new DataReadingException(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.readConfigFileError",
                    propertyPath), ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (Exception ex) {
            }
        }
        setAutoDelete(p);
        setArchivePath(p);
        setEventType(p);
    }

    private void setAutoDelete(Properties p)
            throws DataReadingException {
        String value = p.getProperty(AUTO_DELETE_STREAM_FILE);
        try {
            if (value == null || value.trim().length() == 0) {
                this.isAutoDelFile = true;
            } else {
                value = removeQuote(value);
                if (value.equalsIgnoreCase("n") || value.equalsIgnoreCase("no")) {
                    this.isAutoDelFile = false;
                } else if (value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes")){
                    this.isAutoDelFile = true;
                }else{
                    logger.warn(msgUtil
                            .getMessage("ct.report.dlc.dataloader.parameter.isautodelfile.warn"));
                    this.isAutoDelFile = true;
                }
            }
        } catch (Exception ex) {
            throw new DataReadingException(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.parseParameterError",
                    AUTO_DELETE_STREAM_FILE, ex.getMessage()), ex);
        }
    }

    private void setArchivePath(Properties p) {
        String value = p.getProperty(ARCHIVE_PATH);
        if (value != null && value.trim().length() > 0) {
            this.archivePath = removeQuote(value);
            if (!new File(archivePath).isDirectory()) {
                logger.warn(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.dir.does.not.exist",
                        archivePath));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ARCHIVE_PATH:" + archivePath);
        }
    }

    private void setEventType(Properties p) {
        String value = p.getProperty(EVENT_TYPE);
        if (value != null && value.trim().length() > 0) {
            this.filterEvent = value;
        } else {
            logger.warn(msgUtil
                    .getMessage("ct.report.dlc.dataloader.parameter.eventType.warn"));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("EVENT_TYPE:" + filterEvent);
        }
    }
    private String removeQuote(String line){
        String value = line.trim();
        final String QUOTE = "\"";
        if (value.contains(QUOTE)) {
            return value.substring(value.indexOf(QUOTE) + 1,
                    value.lastIndexOf(QUOTE));
        } else {
            return value;
        }
    }

    /*
     * this function is being used to examine the record and filter out any
     * characters that we don't want
     */
    public String cleanEvent(String rec) {
        return rec.replaceAll(filterStr, "");
    }

    private boolean checkEventType(String record) {
        String eventType = getEventType(record);
        if (eventType == null || eventType == "" || eventType.equals(" ")) {
            return false;
        }
        if (this.filterEvent.contains(eventType)) {
            return true;
        } else {
            return false;
        }
    }

    private String getEventType(String record) {
        String eventType = null;
        try {
            if (record == null || record.equals("")) {
                return null;
            }
            int semicolon = record.indexOf(";");
            if (semicolon > 1) {
                eventType = record.substring(semicolon + 1, semicolon + 2);
            } else {
                logger.error(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.noSemicolonFound", record));
                return null;
            }
        } catch (Exception ex) {
            logger.warn(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.parseRecordError", record), ex);
        }
        return eventType;
    }

    private long transformTime(String str) {
        if (str == null || ! str.contains(":")) {
            return 0;
        }
        long result = 0;
        String[] timeValue = str.split(":");
        if(timeValue.length == 3) {
            int hour = transformInt(timeValue[0]);
            int minute = transformInt(timeValue[1]);
            int second = transformInt(timeValue[2]);
            if (hour < 864000) {
                //During test, there will be data like "resources_used.cput=5125346:51:25"
                //It seems torque has some errors. But it will lead DB feild out of range.
                //So, we will limite the value less than 100 years.
                result = hour * 3600 + minute * 60 + second;
            } else {
                logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.tooLargeTimeValue"));
                result = 9999999999L;
            }
        }
        return result;
    }

    private int transformInt(String str) {
        int result = 0;
        if ("00".endsWith(str)) {
            result = 0;
        } else if (str.startsWith("0")){
            result = Integer.valueOf(str.substring(1));
        } else {
            result = Integer.valueOf(str);
        }
        return result;
    }

    private String transformDate(String str) {
        // input 11/21/2012, result: 2012-11-21
        String result = "";
        String month = str.substring(0, 2);
        String day = str.substring(3, 5);
        String year = str.substring(6);
        result = year + "-" + month + "-" + day;
        return result;
    }

    private int getResourceCPNum(String nodesReq) {
        if (nodesReq == null || nodesReq.isEmpty()) {
            return 0;
        }

        int resCPUNum = 0;
        String[] resTypes = nodesReq.split("\\+");
        resCPUNum = resTypes.length;
        return resCPUNum;
    }

    private Record processReocrd(String line) {
        // Create a record
        Record record = new Record();

        int lineNo = 0;
        try {
            lineNo++;
            String[] values = csvParser.parseLine(line);
            if (values.length < 2) {
                return null;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("bufRecord[" + lineNo + "] = " + line);
            }

            // Get the header
            String[] header = headers.get(tableName);
            if (header == null) {
                logger.warn("No header found for the table '{}'. Ignore the line {}.",
                        tableName, lineNo);
                return null;
            }

            // Set table name for the record
            String timeStr = transformDate(values[0]);
            String[] jobHeaders = values[1].split(";");
            String timeStr2 = jobHeaders[0];
            Timestamp eventTime = Timestamp.valueOf(timeStr + " " + timeStr2);
            String eventType = jobHeaders[1];
            String jobId = (jobHeaders[2]).split("\\.")[0];
            values[1] = jobHeaders[3];

            // Create a hashmap for key-value pairs.
            HashMap<String, String> jobDetailsMap = new HashMap<String, String>();
            int symbolIndex = 0;
            for (int i = 1; i < values.length; i++) {
                symbolIndex = values[i].indexOf("=");
                if(symbolIndex > 0) {
                    jobDetailsMap.put(values[i].substring(0, symbolIndex),
                                      values[i].substring(symbolIndex+1));
                }
            }

            record.put(TABLE_NAME_KEY, tableName);
            record.put("RECORD_TYPE", eventType);
            record.put("TIME_STAMP", eventTime);
            record.put("JOB_ID", jobId);
            record.put("JOB_NAME", jobDetailsMap.get("jobname"));
            record.put("USER_NAME", jobDetailsMap.get("user"));
            record.put("GROUP_NAME", jobDetailsMap.get("group"));
            record.put("QUEUE", jobDetailsMap.get("queue"));
            record.put("OWER_NAME", jobDetailsMap.get("owner"));
            record.put("RESOURCE_LIST_NEEDNODES",
                       jobDetailsMap.get("Resource_List.neednodes"));
            record.put("RESOURCE_LIST_NODECT",
                       jobDetailsMap.get("Resource_List.nodect"));

            long listWalltime = transformTime(jobDetailsMap.get("Resource_List.walltime"));
            record.put("RESOURCE_LIST_WALLTIME", listWalltime);

            String ctimeStr = jobDetailsMap.get("ctime") != null ? jobDetailsMap.get("ctime") : "86400";
            Timestamp cTime = new Timestamp(Long.parseLong(ctimeStr) * 1000);
            record.put("CTIME", cTime);

            String qtimeStr = jobDetailsMap.get("qtime") != null ? jobDetailsMap.get("qtime") : "86400";
            Timestamp qTime = new Timestamp(Long.parseLong(qtimeStr) * 1000);
            record.put("QTIME", qTime);

            String etimeStr = jobDetailsMap.get("etime") != null ? jobDetailsMap.get("etime") : "86400";
            Timestamp eTime = new Timestamp(Long.parseLong(etimeStr) * 1000);
            record.put("ETIME", eTime);

            String stimeStr = jobDetailsMap.get("start") != null ? jobDetailsMap.get("start") : "86400";
            Timestamp startTime = new Timestamp(Long.parseLong(stimeStr) * 1000);
            record.put("START_TIME", startTime);

            int resourceCPNum = getResourceCPNum(
                                jobDetailsMap.get("exec_host"));
            record.put("RESOURCE_CPU_NUM", resourceCPNum);

            long usedWalltime = 0;
            int sessionId = 0;
            int exitStatus = 0;
            long usedCPUT = 0;
            int usedMem = 0;
            long usedVMem = 0;
            Timestamp endTime = null;
            if (eventType.endsWith("E")) {
                sessionId = Integer.parseInt(jobDetailsMap.get("session"));
                endTime = new Timestamp(Long.parseLong(jobDetailsMap.get("end")) * 1000);
                exitStatus = Integer.parseInt(jobDetailsMap.get("Exit_status"));
                usedCPUT = transformTime(jobDetailsMap.get("resources_used.cput"));
                String resourceUsedMem = jobDetailsMap.get("resources_used.mem");
                usedMem = Integer.valueOf(resourceUsedMem.substring(0,
                        resourceUsedMem.indexOf("kb")));
                String resourceUsedUsedVMem = jobDetailsMap.get("resources_used.vmem");
                usedVMem = Long.parseLong(resourceUsedUsedVMem.substring(0,
                        resourceUsedUsedVMem.indexOf("kb")));
                String resourceUsedWalltime = jobDetailsMap.get("resources_used.walltime");
                usedWalltime = transformTime(resourceUsedWalltime);
            }
            record.put("SESSION_ID", sessionId);
            record.put("END_TIME", endTime);
            record.put("EXIT_STATUS", exitStatus);
            record.put("RESOURCE_USED_CPUT", usedCPUT);
            record.put("RESOURCE_USED_MEM", usedMem);
            record.put("RESOURCE_USED_VMEM", usedVMem);
            record.put("RESOURCE_USED_WALLTIME", usedWalltime);

            if (logger.isTraceEnabled()) {
                logger.trace("The record for line {} is: {}", lineNo, record);
            }
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
        return record;
    }

    /* (non-Javadoc)
     * @see com.clustertech.report.dlc.framework.reader.DataReader#hasMoreData()
     */
    @Override
    public boolean hasMoreData() throws DataReadingException {
        boolean ret = true;
        if (hasError == true) {
            ret = false;
        }
        ret = afReader.getHasMoreRecord();
        return ret;
    }
}
