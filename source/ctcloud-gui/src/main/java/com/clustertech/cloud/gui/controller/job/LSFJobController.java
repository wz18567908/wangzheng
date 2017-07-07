package com.clustertech.cloud.gui.controller.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.controller.BaseController;
import com.clustertech.cloud.gui.domain.job.LSFJobEntity;
import com.clustertech.cloud.gui.service.job.LSFJobService;
import com.clustertech.cloud.gui.upload.UploadFileList;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.DataFormatUtils;
import com.clustertech.cloud.gui.utils.Page;
import com.clustertech.cloud.gui.utils.StringUtil;

@Transactional
@RestController
@RequestMapping("/api/job/control")
public class LSFJobController extends BaseController<LSFJobController> implements Serializable {
    private static final long serialVersionUID = 1662200001259614525L;

    @Autowired
    private LSFJobService lsfJobControlService;

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public Map<String, Object> getLSFJobInfoByPrimaryKeys(long jobId, int arrayIndex, String clusterName) {
        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        LSFJobEntity lsfJobEntity = new LSFJobEntity();
        String code = null;
        String message = null;
        try {
            propertyValueMap.put("jobId", jobId);
            propertyValueMap.put("arrayIndex", arrayIndex);
            propertyValueMap.put("clusterName", clusterName);
            lsfJobEntity = lsfJobControlService.getLSFJobInfoByPrimaryKeys(propertyValueMap);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getLSFJobInfoByPrimaryKeys", String.valueOf(jobId),
                    String.valueOf(arrayIndex), String.valueOf(clusterName)));
            code = CloudConstants.LSF_JOB_LIST_CODE;
            message = CloudConstants.LSF_JOB_LIST_MSG;
        }
        return DataFormatUtils.format(code, message, lsfJobEntity);
    }

    @RequestMapping(value = "/jobList", method = RequestMethod.GET)
    public Map<String, Object> getLSFJobInfoPage(int draw, int start, int length, String currentUserName,
            String orderBy, @RequestParam(value = "search[value]") String searchValue,
            @RequestParam(value = "filterData[filter]") String filter,
            @RequestParam(value = "filterData[userName]") String userName,
            @RequestParam(value = "filterData[queue]") String queue,
            @RequestParam(value = "filterData[jobStatus]") String jobStatus,
            @RequestParam(value = "filterData[subtimePassHours]") int subtimePassHours,
            @RequestParam(value = "filterData[endtimePassHours]") int endtimePassHours) {
        Map<String, Object> filterData = new HashMap<String, Object>();
        filterData.put("userName", userName);
        filterData.put("queue", queue);
        filterData.put("jobStatus", jobStatus);
        filterData.put("submitTime", subtimePassHours);
        filterData.put("endTime", endtimePassHours);
        Page<LSFJobEntity> lsfJobInfoPage = new Page<LSFJobEntity>();
        int pageNo = start / length + 1;
        try {
            lsfJobInfoPage = lsfJobControlService.getLSFJobInfoPage(pageNo, length, currentUserName, orderBy,
                    searchValue, filter, filterData);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getLSFJobInfoPage", String.valueOf(pageNo),
                    String.valueOf(length)));
        }
        return DataFormatUtils.formatPage(draw, lsfJobInfoPage);
    }

    @RequestMapping(value = "/queues", method = RequestMethod.GET)
    public Map<String, Object> getLSFJobQueues() {
        List<String> queueList = new ArrayList<String>();
        String code = null;
        String message = null;
        try {
            queueList = lsfJobControlService.getLSFJobQueues();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getLSFJobQueues"));
            code = CloudConstants.LSF_JOB_QUEUE_CODE;
            message = CloudConstants.LSF_JOB_QUEUE_MSG;
        }
        return DataFormatUtils.format(code, message, queueList);
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public Map<String, Object> createLSFJob(HttpServletRequest request, String uploadfs,
            @RequestParam(value = "jobObject") String jobBean) {
        Map<String, Object> submitJobInfo = new HashMap<String, Object>();
        UploadFileList filelist = null;
        String code = null;
        String message = null;
        try {
            LSFJobEntity jobObject = (LSFJobEntity) CommandUtil.JSONToObj(jobBean, LSFJobEntity.class);
            filelist = (UploadFileList) CommandUtil.JSONToObj(uploadfs, UploadFileList.class);
            submitJobInfo = lsfJobControlService.createLsfJob(jobObject, getCloudConfigureInfo(request).getCloudTop(),
                    filelist.getFileItems());
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "createLSFJob", jobBean));
            code = CloudConstants.LSF_JOB_SUBMIT_CODE;
            message = CloudConstants.LSF_JOB_SUBMIT_MSG;
        }
        return DataFormatUtils.format(code, message, filelist.getFileItems(), submitJobInfo);
    }

    @RequestMapping(value = "/{jobAction}", method = RequestMethod.POST)
    public Map<String, Object> control(@PathVariable(value = "jobAction") String jobAction, String jobIds,
            String userName) {
        Map<String, Object> message = new HashMap<String, Object>();
        try {
            message = lsfJobControlService.controlJobs(jobAction, jobIds, userName);
            lsfJobControlService.updateControlledJobs(message, jobAction);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "control", jobAction, jobIds, userName));
        }
        return message;
    }
}
