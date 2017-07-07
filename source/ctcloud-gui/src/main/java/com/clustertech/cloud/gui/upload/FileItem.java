package com.clustertech.cloud.gui.upload;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class FileItem implements Serializable {
    private static final long serialVersionUID = -3331429044176046387L;

    private int id;
    private String name;
    private String type;
    private String host;
    private long size;
    private Date modifyTime;
    private String modifyTimeStr;
    private boolean folder;
    private String owner;
    private String group;
    private String permission;
    private String absolutePath;
    private String escapeAbsolutePath;

    public FileItem() {
    }

    public FileItem(File file) {
        setName(file.getName());
        setSize(file.length());
        setType(file.isFile() ? FileUtils.substringAfterLast(file.getName(), ".") : "");
        setModifyTime(new Date(file.lastModified()));
        setAbsolutePath(file.getAbsolutePath());
        setEscapeAbsolutePath(file.getAbsolutePath());
        setFolder(file.isDirectory());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyTimeStr() {
        return modifyTimeStr;
    }

    public void setModifyTimeStr(String modifyTimeStr) {
        this.modifyTimeStr = modifyTimeStr;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getEscapeAbsolutePath() {
        return escapeAbsolutePath;
    }

    public void setEscapeAbsolutePath(String escapeAbsolutePath) {
        this.escapeAbsolutePath = escapeAbsolutePath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
