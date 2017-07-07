package com.clustertech.cloud.gui.utils;

import java.io.File;

public class CloudConstants {

    // common constant info
    public static final String JDBC_USERNAME = "jdbc.username";
    public static final String JDBC_PASSWORD = "jdbc.password";

    // file and directory info
    public final static String HOMEDIR =File.separator + "home" + File.separator;
    public final static String TMPDIR =File.separator + "tmp" + File.separator;
    public static final String GUI_DIR = File.separator + "gui" + File.separator;
    public static final String GUI_CONF_DIR = GUI_DIR + "conf" + File.separator;
    public static final String GUI_ETC_DIR = GUI_DIR + "etc" + File.separator;
    public static final String CLOUD_CONF = GUI_CONF_DIR + "ctcloud.xml";
    public static final String CLOUD_CONF_MAPPING = GUI_CONF_DIR + "ctcloudMapping.xml";

    // code and error info
    public static final String LSF_JOB_LIST_CODE = "GET_LSF_JOB_LIST_EXCEPTION";
    public static final String LSF_JOB_LIST_MSG = "err.lsf.job.list";
    public static final String LSF_JOB_DETAIL_CODE = "GET_LSF_JOB_DETAIL_EXCEPTION";
    public static final String LSF_JOB_DETAIL_MSG = "err.lsf.job.detail";
    public static final String LSF_JOB_QUEUE_CODE = "GET_LSF_JOB_QUEUE_EXCEPTION";
    public static final String LSF_JOB_QUEUE_MSG = "err.lsf.job.queue";
    public static final String LSF_JOB_SUBMIT_CODE = "GET_LSF_JOB_SUBMIT_EXCEPTION";
    public static final String LSF_JOB_SUBMIT_MSG = "err.lsf.job.submit";
    public static final String FILE_GET_CODE = "GET_REMOTE_FILE_EXCEPTION";
    public static final String FILE_GET_MSG = "err.get.remote.file";
    public static final String FILE_UPLOAD_CODE = "GET_FILE_UPLOAD_EXCEPTION";
    public static final String FILE_UPLOAD_MSG = "err.file.upload";
    public static final String FILE_MERGE_CODE = "GET_FILE_MERGE_EXCEPTION";
    public static final String FILE_MERGE_MSG = "err.file.merge";
    public static final String FILE_CLEAN_CODE = "GET_FILE_CLEAN_EXCEPTION";
    public static final String FILE_CLEAN_MSG = "err.file.clean";

    public static final String REPORT_SETTING_LIST_MSG = "err.report.setting.list";
    public static final String REPORT_LICENSE_DATE_ADD_MSG = "err.report.license.date.add";
    public static final String REPORT_LICENSE_DATE_LIST_MSG = "err.report.license.date.list";
    public static final String REPORT_LICENSE_DATE_DELETE_MSG = "err.report.license.date.delete";
    public static final String REPORT_LICENSE_FILTER_LIST_MSG = "err.report.license.filter.list";
    public static final String REPORT_LICENSE_USER_LIST_MSG = "err.report.license.user.list";
    public static final String REPORT_LICENSE_FEATURE_LIST_MSG = "err.report.license.feature.list";
    public static final String REPORT_LICENSE_CHART_LIST_MSG = "err.report.license.chart.list";

    // enum class
    public enum TimeUnitEnum {
        MONTH,
        DAY,
        HOUR
    }

    public enum OrderByEnum {
        DESC,
        ASC
    }

    public enum ReportFilterEnum {
        USER,
        GROUP,
        PROJECT,
        FEATURE,
        LICENSE,
        APPLICATION
    }

    public enum ResourceEnum {
        FEATURE_PERIOD,
        FEATURE_USAGE,
        FEATURE_PEAK
    }

    public enum LicenseTimeEnum {
        TOTAL_TIME,
        WORK_TIME
    }

    public enum LicenseConfigDateEnum {
        WORKING,
        OVERTIME,
        VACATION
    }
    
    public enum ShareStorageEnum {
        USAGE,
        USED
    }
}
