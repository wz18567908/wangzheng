package com.clustertech.cloud.gui.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.clustertech.cloud.gui.domain.job.LSFJobEntity;

public class DataFormatUtils {
    private static final String JSON_ERROR = "error";
    private static final String JSON_CTMSG = "message";
    private static final String JSON_SUCCESS = "success";
    private static final String JSON_FAILURE = "failure";
    private static final String JSON_MSG = "msg";
    private static final String JSON_CODE = "code";
    private static final String JSON_DATA = "data";
    private static final String SUBMIT_DATA = "submitData";
    private static final String DATA_DRAW = "draw";
    private static final String DATA_TOTAL = "recordsTotal";
    private static final String DATA_FILTERED = "recordsFiltered";
    private static final String CURRENT_PAGENO = "currentPageNo";
    private static final String TOTAL_PAGENO = "totalPageNo";
    public static final String UPDATE_JOBIDS = "updateJobIds";

    public static Map<String, Object> format(String code, String message, Object obj, Object submitJobInfo) {
        Map<String, Object> objectMap = format(code, message, obj);
        objectMap.put(SUBMIT_DATA, submitJobInfo);
        return objectMap;
    }

    public static Map<String, Object> format(String message, Object obj) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put(JSON_ERROR, message);
        objectMap.put(JSON_DATA, obj);
        return objectMap;
    }

    //It will be remove in the future, you can use format(String message, Object obj) function instead.
    public static Map<String, Object> format(String code, String message, Object obj) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        Map<String, String> errorMap = new HashMap<String, String>();
        if (!(code == null || code.isEmpty()) && !(message == null || message.isEmpty())) {
            errorMap.put(JSON_CODE, code);
            errorMap.put(JSON_MSG, message);
        }
        objectMap.put(JSON_ERROR, errorMap);
        objectMap.put(JSON_DATA, obj);
        return objectMap;
    }

    public static Map<String, Object> formatPage(int draw, Page<LSFJobEntity> jobEntity) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put(DATA_DRAW, draw);
        objectMap.put(DATA_TOTAL, jobEntity.getTotalPageNo());
        objectMap.put(DATA_FILTERED, jobEntity.getTotalPageNo());
        objectMap.put(CURRENT_PAGENO, jobEntity.getCurrentPageNo());
        objectMap.put(TOTAL_PAGENO, jobEntity.getTotalPageNo());
        objectMap.put(JSON_DATA, jobEntity.getDataList());
        return objectMap;
    }

    public static Map<String, Object> formatControl(List<String> success, List<String> failure,
            List<String> updateJobIds, String jobAction) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        Map<String, String> messageMap = new HashMap<String, String>();
        String successMsg = null;
        String failureMsg = null;
        if (success.size() > 0) {
            if (!jobAction.equalsIgnoreCase("bpeek")) {
                successMsg = StringUtils.join(success.toArray(), ";").replaceAll("\n", "");
            } else {
                successMsg = StringUtils.join(success.toArray(), "").replaceAll("<< output from stdout >>", "");
            }
        }
        if (failure.size() > 0) {
            failureMsg = StringUtils.join(failure.toArray(), ";").replaceAll("\n", "");
        }
        if (!(successMsg == null || successMsg.isEmpty()) || !(failureMsg == null || failureMsg.isEmpty())) {
            messageMap.put(JSON_SUCCESS, successMsg);
            messageMap.put(JSON_FAILURE, failureMsg);
        }
        objectMap.put(JSON_CTMSG, messageMap);
        if (updateJobIds.size() != 0) {
            objectMap.put(UPDATE_JOBIDS, StringUtils.join(updateJobIds.toArray(), ","));
        } else {
            objectMap.put(UPDATE_JOBIDS, null);
        }
        return objectMap;
    }
}
