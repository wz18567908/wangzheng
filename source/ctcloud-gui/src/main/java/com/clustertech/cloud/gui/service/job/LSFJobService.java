package com.clustertech.cloud.gui.service.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clustertech.cloud.gui.dao.job.LSFJobControlDao;
import com.clustertech.cloud.gui.domain.job.LSFJobEntity;
import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.upload.FileItem;
import com.clustertech.cloud.gui.upload.UploadFileUtil;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.CommandUtil.CommandResult;
import com.clustertech.cloud.gui.utils.DataFormatUtils;
import com.clustertech.cloud.gui.utils.Page;
import com.clustertech.cloud.gui.utils.StringUtil;
import com.clustertech.cloud.gui.utils.TransferToBatch4LSF;
import com.clustertech.cloud.jni.lsf.LSFBatch;
import com.clustertech.cloud.jni.lsf.domian.LSFJobInfo;

@Service
@Transactional
public class LSFJobService {
    public static final String JOB_CREATE_SHELL = "createjob.sh";

    public static final Logger logger = Logger.getLogger(LSFJobService.class);

    @Autowired
    private LSFJobControlDao lsfJobControlDao;

    public List<LSFJobEntity> getLSFJobInfoList() {
        return lsfJobControlDao.getEntityList();
    }

    public LSFJobEntity getLSFJobInfoByPrimaryKeys(Map<String, Object> propertyValueMap) {
        return lsfJobControlDao.getEntityByPrimaryKeys(propertyValueMap);
    }

    public Page<LSFJobEntity> getLSFJobInfoPage(int pageNo, int pageSize, String currentUserName, String orderBy,
            String searchValue, String filter, Map<String, Object> filterData) {
        return lsfJobControlDao.getLSFJobInfoPage(pageNo, pageSize, currentUserName, orderBy, searchValue, filter,
                filterData);
    }

    public List<String> getLSFJobQueues() throws CTCloudException {
        String[] queues = {};
        String cmd = "bqueues -w | awk '$0 !~ /QUEUE_NAME/ {print $1}' | sort";
        CommandResult cmdResult = CommandUtil.transferRunCmmand4User(cmd, "root", null);
        if (cmdResult.getExitCode() != 0) {
            throw new CTCloudException(
                    (null != cmdResult.getStderr()) ? cmdResult.getStderr().toString() : "Failed to get error.");
        } else {
            String queueString = cmdResult.getStdout();
            queues = queueString.split("\n");
        }
        return Arrays.asList(queues);
    }

    public Map<String, Object> createLsfJob(LSFJobEntity job, String shellPath, List<FileItem> uploadfs)
            throws CTCloudException {
        try {
            UploadFileUtil.handleUploadfile4Remote(uploadfs, job);
        } catch (Exception e) {
            throw new CTCloudException(StringUtil.getStackTrace(e));
        }
        List<LSFJobInfo> jobInfoList = new ArrayList<LSFJobInfo>();
        Map<String, Object> submitJobInfo = new HashMap<String, Object>();
        Map<String, String> mapEnv = TransferToBatch4LSF.getBatchEnv(job);
        String script = shellPath + CloudConstants.GUI_ETC_DIR + JOB_CREATE_SHELL;
        CommandResult cmdResult = CommandUtil.transferRunTmpBatch(script, mapEnv, "root");
        int exitValue = cmdResult.getExitCode();
        if (exitValue != 0) {
            CommandUtil.handleErrResult(cmdResult, cmdResult.getStderr());
            throw new CTCloudException("Failed to create job :" + cmdResult.getStderr());
        } else {
            try {
                long jobId = Long.parseLong(cmdResult.getStdout().replaceAll(CommandUtil.LINE_SP, ""));
                jobInfoList = LSFBatch.getInstance().readJobInfo(jobId);
                if (jobInfoList.get(0).getArrayIndexId() == 0) {
                    submitJobInfo.put("jobId", jobInfoList.get(0).getJobId());
                    submitJobInfo.put("arrayIndex", jobInfoList.get(0).getArrayIndexId());
                    submitJobInfo.put("clusterName", jobInfoList.get(0).getClusterName());
                    lsfJobControlDao.duplicateSave(CommandUtil.transferToJob(jobInfoList.get(0), null));
                } else {
                    for (LSFJobInfo jobInfo : jobInfoList) {
                        lsfJobControlDao.duplicateSave(CommandUtil.transferToJob(jobInfo, null));
                    }
                }
            } catch (Exception e) {
                logger.error(StringUtil.formatErrorLogger(e, "createLsfJob", String.format("LSFJobEntity(%s,%s,%s,%s)",
                        job.getJobName(), job.getCommand(), job.getUserName(), job.getQueue()), shellPath));
            }
        }
        return submitJobInfo;
    }

    public Map<String, Object> controlJobs(String jobAction, String jobIds, String user) {
        List<String> ids = new ArrayList<String>();
        List<String> success = new ArrayList<String>();
        List<String> failure = new ArrayList<String>();
        List<String> updateJobIds = new ArrayList<String>();
        if (jobIds != null && !"0".equals(jobIds)) {
            ids = Arrays.asList(jobIds.split(","));
        }
        String cmd = CommandUtil.transferJobAction(jobAction);
        if (!StringUtils.isEmpty(cmd)) {
            for (String jId : ids) {
                try {
                    CommandResult cmdResult = CommandUtil.transferRunCmmand4User(cmd + jId, user, null);
                    if (cmdResult.getStderr() != null && !cmdResult.getStderr().isEmpty())
                        failure.add(cmdResult.getStderr());
                    if (cmdResult.getStdout() != null && !cmdResult.getStdout().isEmpty()) {
                        success.add(cmdResult.getStdout());
                        if (!jobAction.equalsIgnoreCase("bpeek")) {
                            updateJobIds.add(jId);
                        }
                    }
                } catch (Exception e) {
                    if (!StringUtils.isEmpty(e.getMessage()))
                        logger.error(StringUtil.formatErrorLogger(e, "controlJobs", jobAction, jobIds, user));
                }
            }
        }
        return DataFormatUtils.formatControl(success, failure, updateJobIds, jobAction);
    }

    public void updateControlledJobs(Map<String, Object> message, String jobAction) {
        if (message.get(DataFormatUtils.UPDATE_JOBIDS) != null) {
            List<LSFJobInfo> jobInfoList = new ArrayList<LSFJobInfo>();
            List<String> jobIdList = Arrays.asList(message.get(DataFormatUtils.UPDATE_JOBIDS).toString().split(","));
            for (String jobId : jobIdList) {
                jobInfoList = LSFBatch.getInstance().readJobInfo(Long.parseLong(jobId));
                for (LSFJobInfo jobInfo : jobInfoList) {
                    lsfJobControlDao.update(CommandUtil.transferToJob(jobInfo, jobAction));
                }
            }
        }
    }
}
