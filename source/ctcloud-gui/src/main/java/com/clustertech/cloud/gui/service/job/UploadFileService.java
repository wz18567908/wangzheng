package com.clustertech.cloud.gui.service.job;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.upload.RegularFileNameFile;
import com.clustertech.cloud.gui.upload.UploadFileItem;
import com.clustertech.cloud.gui.upload.UploadFileUtil;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.StringUtil;
import com.clustertech.cloud.gui.utils.CommandUtil.CommandResult;

@Service
public class UploadFileService {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileService.class);

    private static final String[] PROGRESSBAR = { "UPLOADING", "DONE" };

    public String uploadFile(UploadFileItem fileItem, String user) throws Exception {

        String progressStep = PROGRESSBAR[1];
        MultipartFile uploadFile = fileItem.getFile();
        if (null == uploadFile) {
            return null;
        }

        String userTempDir = UploadFileUtil.getChunkFileDir(user);
        String fpath = userTempDir + File.separator + fileItem.getFileName();
        int chunLength = fileItem.getChunkLength();
        if (chunLength > 1) {
            fpath += "_" + chunLength + "_" + fileItem.getChunkIndex();
        }
        File tmpFile = new File(fpath);
        uploadFile.transferTo(tmpFile);
        try {
            String destPath = UploadFileUtil.getfileDestPath(fileItem, user);
            progressStep = startToFileUpload(chunLength, tmpFile, fileItem.getHostName(), destPath,
                    fileItem.getFileName(), user);
        } catch (Exception e) {
            // 500
            if (tmpFile != null) {
                tmpFile.delete();
            }
            throw new CTCloudException(e.getMessage());
        } finally {
        }
        return progressStep;
    }

    public String startToFileUpload(int chunkFileLength, File tmpFile, String hostName, String dirPath, String fileName,
            String userName) throws Exception {
        CommandResult ret = null;

        String copyCMD = "";
        tmpFile.setReadable(true, false);
        tmpFile.setWritable(true, false);

        String progressStep = PROGRESSBAR[1];
        if (chunkFileLength > 1) {
            progressStep = PROGRESSBAR[0];
        } else {
            try {
                copyCMD = "cp '" + tmpFile.getAbsolutePath() + "' '" + dirPath + File.separator + "'";
                ret = CommandUtil.transferRunCmmand4User(copyCMD, userName, null);
                if (ret.getExitCode() != 0) {
                    throw new CTCloudException("File upload error:" + ret.getStderr());
                }
            } catch (Exception e) {
                logger.error(
                        StringUtil.formatErrorLogger(e, "startToFileUpload", "File upload error:" + e.getMessage()));
            } finally {
                tmpFile.delete();
            }
        }
        return progressStep;
    }

    public String mergeChunkFile(UploadFileItem fileItem, String user) throws Exception {

        String progressStep = null;
        String userTempDir = UploadFileUtil.getChunkFileDir(user);
        try {
            String destPath = UploadFileUtil.getfileDestPath(fileItem, user);
            progressStep = startToMergeFile(fileItem.getChunkLength(), userTempDir, fileItem.getHostName(), destPath,
                    fileItem.getFileName(), user);
        } catch (Exception e) {
            logger.error(
                    StringUtil.formatErrorLogger(e, "mergeChunkFile", "File merge error:" + e.getMessage()));
        }
        return progressStep;
    }

    public String startToMergeFile(int chunkFileLength, String tmpFilePath, String hostName, String destPath,
            String fileName, String userName) throws Exception {
        CommandResult ret = null;

        String catCMD = null;
        String destFile = tmpFilePath + File.separator + fileName;
        File dFile = new File(destFile);

        String firstFile = destFile + "_" + chunkFileLength + "_0";
        File fFile = new File(firstFile);

        if (fFile.exists()) {
            File tmpPath = new File(tmpFilePath);
            int x = String.valueOf(chunkFileLength).length();
            String pattern = "^" + fileName + "_" + chunkFileLength + "_[0-9]{1," + x + "}+$";
            File[] fns = tmpPath.listFiles(new RegularFileNameFile(pattern));
            try {
                for (int i = 1; i < fns.length; i++) {
                    String tFileName = tmpFilePath + File.separator + fileName + "_" + chunkFileLength + "_" + i;
                    if ((new File(tFileName)).exists()) {
                        catCMD = " cat '" + tFileName + "' >> '" + firstFile + "' ";
                        ret = CommandUtil.transferRunCmmand4User(catCMD, userName, null);

                        if (ret.getExitCode() != 0) {
                            throw new CTCloudException("File merge error:" + ret.getStderr());
                        }
                    } else {
                        throw new CTCloudException("File merge error: chunk file doesn't exists " + tFileName);
                    }
                }

                // change file name
                fFile.renameTo(dFile);
                catCMD = "cp '" + destFile + "' '" + destPath + "' ";
                ret = CommandUtil.transferRunCmmand4User(catCMD, userName, null);
                if (ret.getExitCode() != 0) {
                    throw new CTCloudException("File merge error:" + ret.getStderr());
                }
            } catch (Exception e) {
                throw new CTCloudException("File merge error:" + e.getMessage());
            } finally {
                if (dFile.exists()) {
                    dFile.delete();
                }
                for (File file : fns) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
        return PROGRESSBAR[1];
    }

    public String cleanUploadFile(UploadFileItem fileItem, String user) throws Exception {
        String progressStep = null;
        String userTempDir = UploadFileUtil.getChunkFileDir(user);
        try {
            progressStep = startToCleanUploadFiles(userTempDir, fileItem.getFileName(), user);
        } catch (Exception e) {
            logger.error(
                    StringUtil.formatErrorLogger(e, "cleanUploadFile", "File clean error:" + e.getMessage()));
        }
        return progressStep;
    }

    public String startToCleanUploadFiles(String tempFilePath, String fileNames, String userName) {
        if (!StringUtils.isEmpty(fileNames)) {
            String[] fileNameArray = fileNames.split(",");

            for (String fileName : fileNameArray) {
                File tempPath = new File(tempFilePath);
                String pattern = "^" + fileName + "_[0-9]{1,}_[0-9]{1,}+$";
                File[] fns = tempPath.listFiles(new RegularFileNameFile(pattern));
                for (File file : fns) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
        return PROGRESSBAR[1];
    }
}
