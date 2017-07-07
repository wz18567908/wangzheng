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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.reader.DataReadingException;

public class ControlFileManager {
    private ControlFileBean controlFileBean = null;
    private File controlFile = null;
    private static MessageHelper msgUtil = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/Resources");
    private Logger logger;
    // the key for control file
    private final String FILE_TO_READ_KEY = "FileToRead";
    private final String FIRST_RECORD_KEY = "FirstRecord";
    private final String FILE_POSITION_KEY = "FilePosition";
    private final String FILE_DIR_KEY = "FileDir";

    public ControlFileManager(Logger logger, File controlFile) {
        this.logger = logger;
        this.controlFile = controlFile;
    }

    public ControlFileBean loadControlFile() throws IOException,
            DataReadingException {
        if (!controlFile.exists()) {
            logger.error(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.fileNotExist",
                    controlFile.getAbsolutePath()));
            controlFile.createNewFile();
        }
        if (controlFile.canRead() && controlFile.isFile()) {
            Properties prop = new Properties();
            controlFileBean = new ControlFileBean();
            FileInputStream fis = new FileInputStream(controlFile);
            try {
                prop.load(fis);
            } catch (Exception ex) {
                throw new DataReadingException(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.readConfigFileError",
                        controlFile.getAbsolutePath()), ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
            String fileToRead = prop.getProperty(FILE_TO_READ_KEY);
            String firstrecord = prop.getProperty(FIRST_RECORD_KEY);
            String lineNum = prop.getProperty(FILE_POSITION_KEY);
            String fileDir = prop.getProperty(FILE_DIR_KEY);

            if (fileToRead != null && fileToRead.length() != 0) {
                controlFileBean.setFileToRead(fileToRead);
            }

            if (firstrecord != null && firstrecord.length() != 0) {
                controlFileBean.setFirstRecord(firstrecord);
            }
            if (lineNum != null && lineNum.length() != 0) {
                controlFileBean.setCurrentPos(Long.parseLong(lineNum));
            }
            if (fileDir != null && fileDir.length() != 0) {
                controlFileBean.setFileDir(fileDir);
            }
        } else {
            throw new DataReadingException(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.fileAccessDenied",
                    controlFile.getAbsolutePath()));
        }
        return controlFileBean;
    }

    /**
     * Internal funciton, write the history information of the currentFileBean
     * into temp file. temp.text
     * 
     * @param controlFileBean
     * 
     * @throws Exception
     */
    public void saveControlFile(ControlFileBean controlFileBean)
            throws IOException {
        this.controlFileBean = controlFileBean;
        if (logger.isDebugEnabled()) {
            logger.debug("Write temp file.");
            logger.debug(controlFileBean.toString());
        }
        if (!controlFile.exists()) {
            logger.error(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.fileNotExist",
                    controlFile.getAbsolutePath()));
            controlFile.createNewFile();
        } else if (!controlFile.canWrite()) {
            throw new RuntimeException(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.fileAccessDenied",
                    controlFile.getAbsolutePath()));
        }
        if (controlFileBean.getFirstRecord() == null) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty(FILE_TO_READ_KEY, controlFileBean.getFileToRead());
        prop.setProperty(FIRST_RECORD_KEY, controlFileBean.getFirstRecord());
        prop.setProperty(FILE_POSITION_KEY, "" + controlFileBean.getCurrentPos());
        prop.setProperty(FILE_DIR_KEY, "" + controlFileBean.getFileDir());
        FileOutputStream fos = new FileOutputStream(controlFile);
        try {
            prop.store(fos, "");
        } catch (IOException ex){
            logger.error(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.saveTempFileError",
                    controlFile.getName()));
            throw ex;
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
}

