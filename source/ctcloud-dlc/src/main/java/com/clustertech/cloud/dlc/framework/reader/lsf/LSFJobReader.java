package com.clustertech.cloud.dlc.framework.reader.lsf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.commons.Record;
import com.clustertech.cloud.dlc.framework.commons.RecordList;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.ReaderConfig;
import com.clustertech.cloud.dlc.framework.reader.DataReader;
import com.clustertech.cloud.dlc.framework.reader.DataReadingException;
import com.clustertech.cloud.jni.lsf.LSFBatch;
import com.clustertech.cloud.jni.lsf.domian.LSFJobInfo;

import au.com.bytecode.opencsv.CSVParser;

public class LSFJobReader extends DataReader {
    /**
     * Static variables
     */
    private static final String DELIMITER_KEY = "delimiter";
    private static final String QUOTE_KEY = "quote";
    private final static String ESCAPE_KEY = "escape";
    private static final String JOB_ACCOUNTING_CTL = "lsf_event";
    private static final String LSF_LOG_FILE_PATH = "LSF_LOG_FILE_PATH";
    private static final String JOBEVENTS_PROPERTY = "lsf_jobevents.properties";
    private static final String WORKLOAD_TYPE_JOB = "JOB";
    private static final String WORKLOAD_TYPE_ARRAY = "ARRAY";

    /**
     * STATE_NULL: 0;  PEND: 1;  PSUSP: 2;  RUN: 4;  SSUSP: 8;  USUSP: 16;  EXIT: 32;
     * DONE: 64;  PDONE: 128;  PERR: 256;  WAIT: 512;  UNKWN: 65536;  PROV: 131072
     */
    private static final String JOB_STATE_NULL = "STATE_NULL";
    private static final String JOB_STATE_PEND = "PEND";
    private static final String JOB_STATE_SUSP = "SUSP";
    private static final String JOB_STATE_RUN = "RUN";
    private static final String JOB_STATE_EXIT = "EXIT";
    private static final String JOB_STATE_DONE = "DONE";
    private static final String JOB_STATE_PERR = "PERR";
    private static final String JOB_STATE_WAIT = "WAIT";
    private static final String JOB_STATE_UNKWN = "UNKWN";
    private static final String JOB_STATE_PROV = "PROV";

    /**
     * Non-static variables
     */
    private String tableName;
    private String lsfLogFilePath;
    private boolean moreRecord;
    private CSVParser csvParser;
    private LSFLogFileReader afReader;
    private Map<String, String[]> headers = new HashMap<String, String[]>();

    /**
     * filter string to avoid null string
     */
    private String filterStr = String.valueOf('\0');

    private static MessageHelper msgUtil = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/Resources");

    public LSFJobReader(String name, ReaderConfig config) {
        super(name, config);

        // Get the delimiter from the configuration
        char delimiter = ' ';
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
        char escape = '$';
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
        moreRecord = true;
        initJobAcctProperty();
        if (afReader == null){
            afReader = new LSFLogFileReader(logger, JOB_ACCOUNTING_CTL);
        }
        afReader.initSingleLog();
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#readData()
     */
    @Override
    public Map<String, RecordList> readData() throws DataReadingException {
        Map<String, RecordList> data = new HashMap<String, RecordList>();
        List<LSFJobIdAndIndex> jobIdAndIndexList = new ArrayList<LSFJobIdAndIndex>();
        int totalRead = 0;
        try {

            long lStart = System.currentTimeMillis();

            List<String> lineList = afReader.readSingleLog(new File(lsfLogFilePath));

            if (lineList == null || lineList.isEmpty()) {
                moreRecord = false;
                return null;
            }

            for (int i = 0; i < lineList.size(); i++) {
                String line = null;
                if (null == lineList.get(i)) {
                    continue;
                } else {
                    line = lineList.get(i);
                }
                if (line.indexOf(filterStr) > -1) {
                    logger.warn(msgUtil.getMessage(
                            "ct.report.dlc.dataloader.filterEscapeChar", line));
                    line = cleanEvent(line);
                }

                String[] values = null;
                try {
                    values = csvParser.parseLine(line);
                } catch(IOException e) {
                    logger.error(e.getMessage() + "\n[" + line + "]\n");
                    continue;
                }

                String targetEvent = values[0];
                if (targetEvent.startsWith("#") || values.length < 4) {
                    continue;
                }

                String jobIdStr = values[3];
                if (! (jobEventEnumContains(targetEvent) && isNumeric(jobIdStr))) {
                    continue;
                }

                /**
                 * JOB_STATUS:
                 *   version=10.1; state=196; length=25; index=11;
                 *   version=9.1.3; state=(196|32); length=22; index=11;
                 *   version 10.1; state=(64|32); length=44; index=30;
                 *   version 9.1.3; state=(64|32); length=41; index=30;
                 */
                LSFJobEventsEnum currentJobEvent = LSFJobEventsEnum.valueOf(targetEvent);
                int jobIndex = 0;
                switch(currentJobEvent) {
                case JOB_START:
                    int numExHosts = Integer.parseInt(values[8]);
                    jobIndex = Integer.parseInt(values[13 + numExHosts]);
                    break;
                case JOB_START_ACCEPT:
                    jobIndex = Integer.parseInt(values[6]);
                    break;
                case JOB_EXECUTE:
                    jobIndex = Integer.parseInt(values[10]);
                    break;
                case JOB_STATUS:
                    if (values.length == 22 || values.length == 25) {
                        jobIndex = Integer.parseInt(values[11]);
                    } else if(values.length == 41 || values.length == 44) {
                        jobIndex = Integer.parseInt(values[30]);
                    } else {
                        continue;
                    }
                    break;
                default:
                    break;
                }

                LSFJobIdAndIndex jobIdAndIndex = new LSFJobIdAndIndex();
                jobIdAndIndex.setJobId(Long.parseLong(jobIdStr));
                jobIdAndIndex.setJobIndex(jobIndex);
                if (! isLSFJobExist(jobIdAndIndexList, jobIdAndIndex)) {
                    jobIdAndIndexList.add(jobIdAndIndex);
                }
            }

            RecordList recordList = processReocrd(jobIdAndIndexList, totalRead);
            if (recordList == null || recordList.isEmpty()) {
                moreRecord = false;
                return null;
            }

            data.put(tableName, recordList);

            logger.info(totalRead
                    + " records have been read; Time to read: "
                    + (System.currentTimeMillis() - lStart) + " ms");
        } catch (Exception e) {
            throw new DataReadingException(e);
        } finally {
            moreRecord = false;
        }
        return data;
    }

    private boolean isLSFJobExist(List<LSFJobIdAndIndex> jobList, LSFJobIdAndIndex jobIdAndIndex) {
        for (LSFJobIdAndIndex job : jobList) {
            if (job.equals(jobIdAndIndex)) {
                return true;
            }
        }
        return false;
    }

    private boolean jobEventEnumContains(String targetEvent) {
        for (LSFJobEventsEnum event : LSFJobEventsEnum.values()) {
            String jobEvent = event.name();
            if (jobEvent.equalsIgnoreCase(targetEvent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see com.clustertech.cloud.dlc.framework.reader.DataReader#onDataWritten()
     */
    @Override
    public void onDataWritten() {
        afReader.updateControlFileForSingleLog(new File(lsfLogFilePath));
    }

    private void initJobAcctProperty() throws DataReadingException {
        String propertyPath = AppConfig.getInstance().getProperty(
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
        setLSFLogFilePath(p);
    }

    private void setLSFLogFilePath(Properties p) {
        String absolutePath = p.getProperty(LSF_LOG_FILE_PATH);
        if (isCanReadFile(absolutePath)) {
            lsfLogFilePath = absolutePath.trim();
        } else {
            logger.warn(msgUtil.getMessage(
                    "ct.cloud.dlc.dataloader.dir.does.not.exist", lsfLogFilePath));
        }
    }

    private boolean isCanReadFile(String absolutePath) {
        if (absolutePath == null || absolutePath.trim().isEmpty()) {
            return false;
        }
        File file = new File(absolutePath);
        return (file.isFile() && file.canRead());
    }

    public String cleanEvent(String rec) {
        return rec.replaceAll(filterStr, "");
    }

   private RecordList processReocrd(List<LSFJobIdAndIndex> jobIdAndIndexList, int totalRead) {
      RecordList recordList = new RecordList();
      for (LSFJobIdAndIndex job : jobIdAndIndexList) {
         try {
            List<LSFJobInfo> lsfJobInfoList = LSFBatch.getInstance().readJobInfo(job.getJobId());
            if (lsfJobInfoList == null || lsfJobInfoList.isEmpty()) {
               continue;
            } else {
                recordList.addAll(getRecordList(lsfJobInfoList, totalRead, job.getJobIndex()));
            }
         } catch (Throwable th) {
            logger.error(th.getLocalizedMessage());
         }
      }
      return recordList;
   }

    private RecordList getRecordList(List<LSFJobInfo> jobInfoList, int totalRead, int jobIndex) {
        RecordList recordList = new RecordList();

        for (LSFJobInfo jobInfo : jobInfoList) {
            Record record = new Record();
            int jobArrayIndex = jobInfo.getArrayIndexId();
            if (! (jobIndex == 0 || jobArrayIndex == jobIndex)) {
                continue;
            }

            record.put("JOB_ID", jobInfo.getJobId());
            record.put("JOB_ARRAY_INDEX", jobArrayIndex);
            record.put("JOB_NAME", jobInfo.getJobName());
            record.put("USER_NAME", jobInfo.getUserName());
            record.put("QUEUE", jobInfo.getQueue());
            record.put("SUBMIT_TIME", formatTime(jobInfo.getSubmitTime()));
            record.put("START_TIME", formatTime(jobInfo.getStartTime()));
            record.put("END_TIME", formatTime(jobInfo.getEndTime()));
            record.put("EXEC_HOSTSTR", formatExecHosts(jobInfo.getExecHosts()));
            record.put("FROM_HOST", jobInfo.getFromHost());
            record.put("COMMAND", jobInfo.getCommand());
            record.put("PROJECT_NAME", jobInfo.getProjectName());
            record.put("JOB_STATUS", convterJobStatus(jobInfo.getJobStatus()));
            record.put("EXIT_STATUS", Integer.toString(jobInfo.getExitStatus()));
            record.put("CLUSTER_NAME", jobInfo.getClusterName());
            record.put("NUM_PROCESSORS", jobInfo.getNumProcessors());

            if (jobArrayIndex == 0) {
                record.put("WORKLOAD_TYPE", WORKLOAD_TYPE_JOB);
            } else {
                record.put("WORKLOAD_TYPE", WORKLOAD_TYPE_ARRAY);
            }

            recordList.add(record);
            totalRead ++;
        }
        return recordList;
    }

    private String convterJobStatus(int status) {
        String result = "";
        switch(status) {
        case 0:
            result = JOB_STATE_NULL;
            break;
        case 1:
            result = JOB_STATE_PEND;
            break;
        case 2:
        case 8:
        case 16:
            result = JOB_STATE_SUSP;
            break;
        case 4:
            result = JOB_STATE_RUN;
            break;
        case 32:
            result = JOB_STATE_EXIT;
            break;
        case 64:
        case 128:
        case 192:    // PDONE + DONE = 128 + 64
        case 320:    // PERR + DONE = 256 + 64
        case 576:    // WAIT + DONE = 512 + 64
            result = JOB_STATE_DONE;
            break;
        case 256:
            result = JOB_STATE_PERR;
            break;
        case 512:
            result = JOB_STATE_WAIT;
            break;
        case 65536:
            result = JOB_STATE_UNKWN;
            break;
        case 131072:
            result = JOB_STATE_PROV;
            break;
        default:
            result = String.valueOf(status);
            break;
        }
        return result;
    }

    private String formatExecHosts(Object[] objs) {
        StringBuffer result = new StringBuffer();
        if (objs == null || objs.length < 1) {
            return "";
        }
        for (Object execHost : objs) {
            result.append(execHost.toString()).append(",");
        }
        return result.substring(0, result.length() - 1);
    }

    private Date formatTime(long time){
        if (time == 0) {
            return null;
        }
        GregorianCalendar gc = new GregorianCalendar();
        time = time * 1000;
        gc.setTimeInMillis(time);
        return new Date(time);
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("^[1-9]\\d*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.clustertech.report.dlc.framework.reader.DataReader#hasMoreData()
     */
    @Override
    public boolean hasMoreData() throws DataReadingException {
        return moreRecord;
    }
}
