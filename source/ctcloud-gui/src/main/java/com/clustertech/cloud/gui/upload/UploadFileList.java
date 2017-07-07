package com.clustertech.cloud.gui.upload;

import java.io.Serializable;
import java.util.List;

public class UploadFileList implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<FileItem> fileItems;

    public List<FileItem> getFileItems() {
        return fileItems;
    }

    public void setFileItems(List<FileItem> fileItems) {
        this.fileItems = fileItems;
    }

}
