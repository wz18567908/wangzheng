package com.clustertech.cloud.gui.upload;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.multipart.MultipartFile;

public class UploadFileItem implements Serializable {
    private static final long serialVersionUID = -7709242104167092289L;

    private MultipartFile file;
    private String hostName;
    private String dest;
    private String fileName;
    private String appName;
    private int chunkIndex;
    private int chunkLength;

    public UploadFileItem() {
    }

    public UploadFileItem(MultipartFile file, String hostName, String dest, String fileName, String appName,
            int chunkIndex, int chunkLength) {
        super();
        this.file = file;
        this.hostName = hostName;
        this.dest = dest;
        this.fileName = fileName;
        this.appName = appName;
        this.chunkIndex = chunkIndex;
        this.chunkLength = chunkLength;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public int getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(int chunkLength) {
        this.chunkLength = chunkLength;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
