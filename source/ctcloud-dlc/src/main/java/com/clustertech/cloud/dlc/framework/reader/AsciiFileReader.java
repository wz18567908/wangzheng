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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.CompressFileUtil;
import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.config.AppConfig;

public class AsciiFileReader {
    private String firstLine = null;

    /** The buffer size for each read. */
    private final int READ_BUFFER_SIZE = 1024 * 1024;

    /** An instance of FileBean includes the current log file information */
    private ControlFileBean controlFileBean = null;
    private ControlFileManager cfManager = null;

    /** a flag to set have more data will be loaded. */
    protected boolean moreRecord = false;
    private Logger logger;

    /** Used to get message from properties file */
    private static MessageHelper msgUtil = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/Resources");

    /**
     * The control file object, it storage the runtime information of
     * dataloader.Eg. file name, first line and last position.
     */
    protected File controlFile = null;
    private long currentPos;
    // the file which be reading currently
    private File targetFile;
    private String controlFileName = null;
    // the full path which will be parsed load.
    private String filePath = null;
    // the base file which need to be loaded
    private File baseFile;
    // backup steam file location
    protected String archivePath;
    protected boolean isAutoDelFile = false;
    protected boolean archiveEnable = false;

    /**
     * @param controlFile
     *            the control file name for file loader.
     * @param filePath
     *            the files will be loaded under the filePath.
     */
    public AsciiFileReader(Logger logger, String controlFile, String filePath)
            throws DataReadingException {
        this.logger = logger;
        this.controlFileName = controlFile;
        this.filePath = filePath;
    }

    public void init() throws DataReadingException {
        readControlFile();
        if (controlFile.exists()) {
            if (!controlFile.canRead() || !controlFile.canWrite()) {
                throw new RuntimeException(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.fileAccessDenied",
                        controlFile.getAbsolutePath()));
            }
        }
        // get all files that the name >= targetFile.getName()
        int filterDate = 0;
        if (this.targetFile != null && this.targetFile.exists()) {
            filterDate = Integer.parseInt(this.targetFile.getName());
        }
        List<File> tmpList = getAvailFiles(this.filePath, filterDate);
        int filenumber = (tmpList == null) ? 0 : tmpList.size();
        moreRecord = (filenumber > 0) ? true : false;
        if (!moreRecord) {
            logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                    targetFile.getAbsolutePath()));
        }
    }

    private void resetControlFile() throws DataReadingException {
        try {
            if (targetFile != null) {
                if (firstLine == null) {
                    firstLine = readFirstLine(targetFile);
                }
                controlFileBean.setFirstRecord(firstLine);
                controlFileBean.setFileToRead(targetFile.getAbsolutePath());
                controlFileBean.setFileDir(targetFile.getParent());
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

    /**
     * compare class to compare two files according file name
     */
    static class FileCompare implements Comparator<File> {
        public int compare(File f1, File f2) {
            return Integer.valueOf(f1.getName()).compareTo(
                    Integer.valueOf(f2.getName()));
        }
    }

    private int findTargetFileIdx(List<File> orderedFileList) throws Exception {
        int i;
        String targetFL = this.controlFileBean.getFirstRecord();
        String targetFN = this.controlFileBean.getFileToRead();
        if (targetFile.exists() && checkFirstLine(targetFile)) {
            for (i = orderedFileList.size() - 1; i >= 0; i--) {
                if (this.targetFile.getName().equals(orderedFileList.get(i).getName())) {
                    break;
                }
            }
        } else {
            for (i = orderedFileList.size() - 1; i >= 0; i--) {
                File tmpFile = orderedFileList.get(i);
                if (!tmpFile.exists()) {
                    logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                            tmpFile.getAbsolutePath()));
                    continue;
                }
                String curFL = readFirstLine(orderedFileList.get(i));
                // can perfectly match
                if (targetFL.equalsIgnoreCase(curFL)) {
                    break;
                }
                // else find the last file which event time > recorded event
                // time
                else {
                    long targetET = getEventTime(targetFL);
                    long curET = getEventTime(curFL);
                    if (targetET > curET) {
                        i = i + 1;
                        this.currentPos = 0;
                        logger.warn(msgUtil
                                .getMessage("ct.report.dlc.dataloader.fileIsMissing",targetFN));
                        break;
                    }
                }
            }
        }
        // all files are newer then target one, read from old to new;
        if (i < 0) {
            i = 0;
            this.currentPos = 0;
            logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileIsMissing",targetFN));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("The Idx of target fileis:" + i);
        }
        return i;
    }

    private void removeFile(File file) {
        boolean READY_TO_REMOVE = true;
        if (!file.canWrite()) {
            logger.warn("ct.report.dlc.dataloader.fileAccessDenied",
                    file.getAbsolutePath());
            return;
        }
        if (this.archiveEnable) {
            READY_TO_REMOVE = backupStreamFile(file);
        }
        if (!READY_TO_REMOVE) {
            logger.warn("ct.report.dlc.dataloader.pauseDeleteFile",
                    file.getAbsolutePath());
            return;
        }
        if (file.delete()) {
            logger.info(msgUtil.getMessage("ct.report.dlc.dataloader.deleteFile",
                    file.getAbsolutePath()));
        } else {
            logger.error(msgUtil.getMessage(
                    "ct.report.dlc.dataloader.failedDeleteFile",
                    file.getAbsolutePath()));
        }
    }

    /**
     * Read the first line with given file object.
     * @param file
     *            the file object that need to be read
     * @return the first line, if without the first line, return null.
     * @throws DataReadingException
     */
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

    /**
     * get all files object return File objects array
     */
    private List<File> getAvailFiles(String dirStr, int filterDate) {
        File dir = new File(dirStr);
        File[] filearray = null;
        final int date = filterDate;
        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (isNumeric(name)) {
                    return !name.contains(".") && Integer.parseInt(name) >= date
                            && (new File(dir + File.separator + name).length() > 0);
                } else {
                    return false;
                }
            }
        };
        filearray = dir.listFiles(fileFilter);
        int filenumber;
        if (filearray == null) {
            return null;
        } else {
            filenumber = filearray.length;
        }
        List<File> list = new ArrayList<File>();
        for (int i = 0; i < filenumber; i++) {
            if (isNumeric(filearray[i].getName())) {
                list.add(filearray[i]);
            }
        }
        return list;
    }

    /**
     * check a string is Number string
     */
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private boolean backupStreamFile(File targetFile) {
        String fromPath = targetFile.getAbsolutePath();
        String toPath = this.archivePath + File.separator + targetFile.getName() + ".ZIP";
        CompressFileUtil compress = new CompressFileUtil();
        try {
            compress.CompressFile(fromPath, toPath);
            logger.info(msgUtil.getMessage("ct.report.dlc.dataloader.backupFile", fromPath,
                    archivePath));
            return true;
        } catch (IOException e) {
            logger.error(msgUtil.getMessage("ct.report.dlc.dataloader.failedBackupFile",
                    fromPath, archivePath));
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * check if the first line equals to that in control file.
     * @return true if equals, otherwise false.
     * @throws DataReadingException
     */
    private boolean checkFirstLine(File file) throws DataReadingException {
        // Read the first line of current data file
        String line = readFirstLine(file);
        if (this.firstLine != null && line != null && (this.firstLine.equals(line))) {
            return true;
        }
        logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.firstLineDoesNotMatch",
                file.getAbsolutePath()));
        return false;
    }

    /**
     * Extract the event time stamp from the event record. the event time is the
     * third field in the record
     * @throws Exception
     */
    private long getEventTime(String record) throws Exception {
        long UTC = 0;
        try {
            if (record == null || record.equals("")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot parse the record:  " + record);
                }
                return 0;
            }
            String str = record.substring(0, record.indexOf(";")).substring(0,
                    record.indexOf(" "));
            String month = str.substring(0, 2);
            String day = str.substring(3, 5);
            String year = str.substring(6);
            String time = record.substring(record.indexOf(" "),
                    record.indexOf(";"));
            UTC = Timestamp.valueOf(year + "-" + month + "-" + day + time).getTime();
        } catch (Exception e) {
            logger.error("There is exception in get event time." + e);
        }
        return UTC;
    }

    private void readControlFile() throws DataReadingException {
        // set the control file
        controlFile = new File(AppConfig.getInstance().getProperty(
                AppConfig.ConfigKey.WORKING_DIR)
                + File.separator + this.controlFileName + ".ctl");
        logger.debug("The control file path is: " + controlFile.getAbsolutePath());
        if (controlFile.exists()) {
            if (!controlFile.canRead() || !controlFile.canWrite()) {
                throw new RuntimeException(msgUtil.getMessage(
                        "ct.report.dlc.dataloader.fileAccessDenied",
                        controlFile.getAbsolutePath()));
            }
        }
        // If control file does not exists, create it
        if (!controlFile.exists()) {
            try {
                File controlParent = controlFile.getParentFile();
                if (controlParent != null && !controlParent.exists()) {
                    controlParent.mkdir();
                }
                controlFile.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        cfManager = new ControlFileManager(logger, controlFile);
        try {
            controlFileBean = cfManager.loadControlFile();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.currentPos = this.controlFileBean.getCurrentPos();
        if (this.controlFileBean.getFileToRead() != null) {
            this.targetFile = new File(this.controlFileBean.getFileToRead());
        } else {
            this.targetFile = null;
        }
        this.firstLine = this.controlFileBean.getFirstRecord();
    }

    public List<String> read() throws Exception {
        String flineOfBaseFile = null;
        List<String> vec = new ArrayList<String>();
        int filterDate = 0;
        if (this.targetFile != null && this.targetFile.exists()) {
            filterDate = Integer.parseInt(this.targetFile.getName());
        }
        List<File> fileList = getAvailFiles(this.filePath, filterDate);
        int filenumber;
        if (fileList == null) {
            filenumber = 0;
        } else {
            filenumber = fileList.size();
        }
        // Sort file according file name
        Collections.sort(fileList, new FileCompare());
        int iRead = 0;
        if (filenumber <= 0) {
            logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                    targetFile.getParent()));
            return null;
        } else {
            baseFile = fileList.get(0);
        }
        if (baseFile.exists()) {
            flineOfBaseFile = readFirstLine(baseFile);
        } else {
            logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                    baseFile.getAbsolutePath()));
        }

        if (logger.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filenumber; i++) {
                File file = (File) fileList.get(i);
                sb.append(file.getName() + " ");
            }
            logger.debug("The stream file list according to the date sequence:  " + sb.toString());
        }
        /* begin to read files from sorted list */
        // case 1: there are no control file or FileToRead is null
        int i = 0;
        if (this.targetFile == null) {
            iRead = readfile(vec, (File) fileList.get(i));
        }
        // case 2: there are control file available to read
        else {
            i = findTargetFileIdx(fileList);
            // all files has been read
            if (i == filenumber) {
                iRead = -1;
            } else {
                if (!fileList.get(i).exists()) {
                    logger.warn(msgUtil.getMessage("ct.report.dlc.dataloader.fileNotExist",
                            fileList.get(i).getAbsolutePath()));
                    return null;
                }
                iRead = readfile(vec, (File) fileList.get(i));
            }
        }
        if (iRead == -1) {
            // finish read last file
            if (i >= filenumber - 1) {
                this.moreRecord = false;
            } else {
                // reset control file if has more then two file to read
                if (i < filenumber - 1) {
                    this.targetFile = fileList.get(i + 1);
                    this.currentPos = 0;
                    this.firstLine = readFirstLine(targetFile);
                }
                // else going to read last file
                else {
                    this.targetFile = fileList.get(i + 1);
                    this.currentPos = 0;
                    if (this.baseFile.getName().equals(this.targetFile.getName())) {
                        this.firstLine = flineOfBaseFile;
                    } else {
                        this.firstLine = readFirstLine(targetFile);
                    }
                }
                this.moreRecord = true;
                // delete the file
                if (isAutoDelFile) {
                    removeFile(fileList.get(i));
                }
            }
        } else {
            this.targetFile = fileList.get(i);
            this.firstLine = readFirstLine(targetFile);
            // check whether the BASE_FILE has been switched if reading
            // BASE_FILE
            if (this.baseFile.getName().equals(this.targetFile.getName())) {
                if (isBaseFileSwitch()) {
                    // data roll back
                    this.currentPos -= iRead;
                    vec = null;
                }
            }
            this.moreRecord = true;
        }
        return vec;
    }

    private boolean isBaseFileSwitch() throws DataReadingException {
        if (!baseFile.exists()) {
            return true;
        } else if (!this.firstLine.equals(readFirstLine(baseFile))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * unit function: read records from files
     */
    private int readfile(List<String> vec, File file)
            throws DataReadingException {
        String line = null;
        logger.debug("begin to read record from filepath:"
                + file.getAbsolutePath());
        BufferedInputStream bis = null;
        BufferedReader br = null;
        int iReaded = 0;
        int tmpReaded = 0;
        try {
            int buffersize = this.READ_BUFFER_SIZE;
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
                    vec.add(line);
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
                                if (vec.size() > 0) {
                                    line = vec.remove(vec.size() - 1);
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
                        vec.add(line);
                    } else if (vec.size() == 0) {
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

    /**
     * @return the moreRecord
     * @throws DataReadingException
     */
    public void updateControlFile() {
        try {
            resetControlFile();
        } catch (DataReadingException e) {
            logger.error("There is DataReadingException in reset control file."
                    + e);
        }
    }

    public void setAutoDelFile(boolean isAutoDelFile) {
        this.isAutoDelFile = isAutoDelFile;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
        if (this.archivePath != null) {
            this.archiveEnable = true;
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean getHasMoreRecord() throws DataReadingException {
        return this.moreRecord;
    }
}