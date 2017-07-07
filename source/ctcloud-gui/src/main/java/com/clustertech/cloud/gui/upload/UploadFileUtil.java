package com.clustertech.cloud.gui.upload;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.clustertech.cloud.gui.domain.job.LSFJobEntity;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.StringUtil;
import com.clustertech.cloud.gui.utils.CommandUtil.CommandResult;

public class UploadFileUtil {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    private static final String SERVERHOST = "server";

    public static final String BLANK_QUOTE = " ";

    public static String createTmpUploadDir(String userName, String jobAppName) {
        CommandResult ret = null;

        String dirName = tmpUploadDirectory(userName, jobAppName);
        File file;
        try {
            file = new File(dirName);
            if (!file.isDirectory()) {
                ret = CommandUtil.transferRunCmmand4User("mkdir -p \'" + dirName + "\'", userName, null);
                if (ret.getExitCode() != 0) {
                    logger.error("create upload tmp directory fail (" + dirName + "):    " + ret.getStderr());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error(StringUtil.formatErrorLogger(e, "createTmpUploadDir", userName, jobAppName, dirName));
        }
        return dirName;
    }

    public static String getChunkFileDir(String userName) {
        CommandResult ret = null;
        String destDir = CloudConstants.HOMEDIR + userName;
        File file;
        try {
            file = new File(destDir);
            if (!file.isDirectory()) {
                ret = CommandUtil.transferRunCmmand4User("mkdir -p \'" + destDir + "\'", userName, null);
            }
            if (null != ret && ret.getExitCode() != 0) {
                logger.error("Job submission directory: " + ret.getStderr());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(StringUtil.formatErrorLogger(e, "getChunkFileDir", userName, destDir));
        }
        return destDir;
    }

    public static String todayTime() {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date());
    }

    public static String tmpUploadDirectory(String userName, String jobAppName) {
        String dirName = CloudConstants.HOMEDIR + userName + File.separator;
        dirName += "tmp_" + todayTime() + File.separator;
        return dirName;
    }

    public static String getfileDestPath(UploadFileItem fileItem, String user) {
        String destPath = fileItem.getDest();
        if (StringUtils.isEmpty(destPath)) {
            try {
                destPath = UploadFileUtil.createTmpUploadDir(user, fileItem.getAppName());
            } catch (Exception e) {
                logger.error(StringUtil.formatErrorLogger(e, "getfileDestPath",
                        "create directory <" + destPath + "> failed. Reason: ", destPath));
                return e.getMessage();
            }
        }
        return destPath;
    }

    public static String getfileDestPath(String user, String jobName) {
        String destPath = null;
        if (StringUtils.isEmpty(destPath)) {
            try {
                destPath = UploadFileUtil.createTmpUploadDir(user, jobName);
            } catch (Exception e) {
                logger.error(StringUtil.formatErrorLogger(e, "getfileDestPath",
                        "create directory <" + destPath + "> failed. Reason: ", destPath));
                return e.getMessage();
            }
        }
        return destPath;
    }

    public static void handleUploadfile4Remote(List<FileItem> uploadfs, LSFJobEntity job) throws Exception {
        CommandResult result = null;
        StringBuffer cmdEnv = new StringBuffer();
        List<FileItem> remotefs = getRemoteUploadfiles(uploadfs);
        if (null != remotefs && !remotefs.isEmpty()) {
            String targetPath = getfileDestPath(job.getUserName(), job.getJobName());
            cmdEnv.append("cp -rfp ");
            for (FileItem fm : remotefs) {
                cmdEnv.append(handleFilePath(fm.getAbsolutePath())).append(BLANK_QUOTE);
            }
            cmdEnv.append(targetPath);
            try {
                result = CommandUtil.transferRunCmmand4User(cmdEnv.toString(), job.getUserName(), null);
                if (result.getExitCode() != 0) {
                    logger.error("copy remote file to < " + targetPath + "> : reason:" + result.getStderr());
                }
            } catch (Exception e) {
                logger.error(StringUtil.formatErrorLogger(e, "handleUploadfile4Remote",
                        "copy remote file to < " + targetPath + "> failed"));
            }
        }
    }

    private static String handleFilePath(String filePath) {
        if (!StringUtils.isEmpty(filePath) && filePath.endsWith(File.separator)) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        return filePath != null ? filePath : "";
    }

    public static List<FileItem> getRemoteUploadfiles(List<FileItem> uploadfs) {
        List<FileItem> remotefs = new ArrayList<FileItem>();
        if (null != uploadfs && !uploadfs.isEmpty()) {
            for (FileItem fm : uploadfs) {
                if (SERVERHOST.equals(fm.getHost())) {
                    remotefs.add(fm);
                }
            }
        }
        return remotefs;
    }
}
