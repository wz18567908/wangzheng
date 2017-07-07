package com.clustertech.cloud.dlc.framework.reader.lsf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.reader.ControlFileBean;
import com.clustertech.cloud.dlc.framework.reader.ControlFileManager;
import com.clustertech.cloud.dlc.framework.reader.DataReadingException;

public class LSFLogFileReader {
    private long currentPos;
    private String firstLine;
    private String controlFileName;
    private Logger logger;
    private File targetFile;
    private File controlFile;
    private ControlFileManager cfManager;
    private ControlFileBean controlFileBean;

    /** The buffer size for each read: 1MB */
    private static final int READ_BUFFER_SIZE = 1024 * 1024;

    /** Used to get message from properties file */
    private static MessageHelper msgUtil = new MessageHelper("com/clustertech/cloud/dlc/framework/Resources");

    public LSFLogFileReader(Logger logger, String controlFileName)
            throws DataReadingException {
        this.logger = logger;
        this.controlFileName = controlFileName;
    }

    public void initSingleLog() throws DataReadingException {
        readControlFile();
        if (controlFile.exists()) {
            if (!controlFile.canRead() || !controlFile.canWrite()) {
                throw new RuntimeException(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.fileAccessDenied", controlFile.getAbsolutePath()));
            }
        }
    }

    private void resetControlFileForSingleLog(File logFile) throws DataReadingException {
        try {
            if (logFile != null) {
                if (firstLine == null) {
                    firstLine = readFirstLine(logFile);
                }
                controlFileBean.setFirstRecord(firstLine);
                controlFileBean.setFileToRead(logFile.getAbsolutePath());
                controlFileBean.setFileDir(logFile.getParent());
                controlFileBean.setCurrentPos(currentPos);
                cfManager.saveControlFile(controlFileBean);
            }
        } catch (DataReadingException e) {
            if (targetFile.exists()) {
                logger.error(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.readFirstLineError",
                        targetFile.getAbsolutePath()), e);
            } else {
                logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                        targetFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private String readFirstLine(File file) throws DataReadingException {
        String ret = null;
        RandomAccessFile raf = null;
        int retrytime = 3;
        try {
            for (int i = 0; i < retrytime; i++) {
                if (file.exists()) {
                    raf = new RandomAccessFile(file, "r");
                    ret = raf.readLine();
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        // do not handle now.
                    }
                }
            }
        } catch (IOException ex) {
            throw new DataReadingException(ex);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    // do not handle now.
                }
            }
        }
        // If the ret is null, that means the file is empty file.
        if (ret == null) {
            logger.info(msgUtil.getMessage("ct.report.dlc.dataloader.fileIsEmpty",
                    file.getAbsolutePath()));
        }
        return ret;
    }

    private void readControlFile() throws DataReadingException {
        controlFile = new File(AppConfig.getInstance().getProperty(
                AppConfig.ConfigKey.WORKING_DIR) + File.separator + controlFileName + ".ctl");

        if (! controlFile.exists()) {
            try {
                File controlParent = controlFile.getParentFile();
                if (controlParent != null && !controlParent.exists()) {
                    controlParent.mkdir();
                }
                controlFile.createNewFile();
            } catch (IOException ex) {
                throw new DataReadingException(ex);
            }
        } else {
            if (!controlFile.canRead() || !controlFile.canWrite()) {
                throw new DataReadingException(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.fileAccessDenied", controlFile.getAbsolutePath()));
            }
        }

        cfManager = new ControlFileManager(logger, controlFile);
        try {
            controlFileBean = cfManager.loadControlFile();
        } catch (Exception ex) {
            throw new DataReadingException(ex);
        }

        currentPos = controlFileBean.getCurrentPos();
        if (controlFileBean.getFileToRead() != null) {
            targetFile = new File(controlFileBean.getFileToRead());
        } else {
            targetFile = null;
        }
        firstLine = controlFileBean.getFirstRecord();
    }

    public List<String> readSingleLog(File file) throws Exception {
        List<String> lineList = new ArrayList<String>();
        readfile(lineList, file);
        if (! lineList.isEmpty()) {
            firstLine = lineList.get(0);
        }
        return lineList;
    }

    private int readfile(List<String> lineList, File file)
            throws DataReadingException {
        String line = null;
        logger.debug("begin to read record from filepath:"
                + file.getAbsolutePath());
        BufferedInputStream bis = null;
        BufferedReader br = null;
        int iReaded = 0;
        int tmpReaded = 0;
        try {
            int buffersize = READ_BUFFER_SIZE;
            int incbuffersize = 4 * buffersize;
            byte[] bytes;
            byte[] tmp;
            bis = new BufferedInputStream(new FileInputStream(file));
            ByteArrayInputStream bais = null;
            logger.debug("currentPos=" + currentPos);
            // 4 times increase the buffer size
            bytes = new byte[incbuffersize];
            long skiped = bis.skip(currentPos);
            currentPos = (currentPos != skiped) ? skiped : currentPos;
            for (int i = 0; i < 4; i++) {
                tmp = new byte[buffersize];
                tmpReaded = bis.read(tmp);
                if (tmpReaded == -1) {
                    iReaded = (iReaded == 0) ? -1 : iReaded;
                    break;
                }
                System.arraycopy(tmp, 0, bytes, iReaded, tmpReaded);
                iReaded += tmpReaded;
                tmp = null;
            }
            logger.debug("iReaded=" + iReaded);
            if (iReaded != -1) {
                bais = new ByteArrayInputStream(bytes, 0, iReaded);
                br = new BufferedReader(new InputStreamReader(bais));

                while ((line = br.readLine()) != null) {
                    lineList.add(line);
                }
                boolean bRemovedLastLine = false;
                int i = incbuffersize - 1;
                for (; i >= 0; i--) {
                    if (bytes[i] == 0) {
                        continue;
                    } else {
                        if (bytes[i] != '\n' && bytes[i] != '\r') {
                            // remove one time when last line is not completely
                            // record
                            if (!bRemovedLastLine) {
                                if (lineList.size() > 0) {
                                    line = lineList.remove(lineList.size() - 1);
                                }
                                bRemovedLastLine = true;
                            }
                        } else {
                            currentPos += i + 1;
                            break;
                        }
                    }
                }

                if (i == -1) {
                    if (iReaded == incbuffersize) {
                        // If in the buffer does not exist '\n' or '\r' and the
                        // read size equals buffer size, this means this record
                        // too large, log a error message.
                        logger.error(msgUtil
                                .getMessage("ct.report.dlc.dataloader.recordTooLarge"));
                        currentPos += iReaded;
                        lineList.add(line);
                    } else if (lineList.size() == 0) {
                        //This file has been completely read.
                        //The last line is read but is not a completed record
                        //set iReaded = -1 to read next file.
                        logger.error(msgUtil
                                .getMessage("ct.report.dlc.dataloader.fileNotEndNormal", file, line));
                        iReaded = -1;
                    }
                }
            }
            if (iReaded == -1) {
                logger.info(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.finishReadingFile",
                        file.getAbsolutePath()));
            }
        } catch (IOException ex) {
            throw new DataReadingException(ex);
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(bis);
        }
        return iReaded;
    }

    public void updateControlFileForSingleLog(File logFile) {
        try {
            resetControlFileForSingleLog(logFile);
        } catch (DataReadingException e) {
            logger.error("There is DataReadingException in reset control file." + e);
        }
    }
}
