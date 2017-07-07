package com.clustertech.cloud.gui.controller.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.controller.BaseController;
import com.clustertech.cloud.gui.service.job.BrowserFileService;
import com.clustertech.cloud.gui.service.job.UploadFileService;
import com.clustertech.cloud.gui.upload.FileItem;
import com.clustertech.cloud.gui.upload.UploadFileItem;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.DataFormatUtils;
import com.clustertech.cloud.gui.utils.StringUtil;

@RestController
@RequestMapping("/api/file/control")
public class FileUploadController extends BaseController<FileUploadController> implements Serializable {

    private static final long serialVersionUID = -1249972852948042706L;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private BrowserFileService browserfileService;

    @RequestMapping(value = "/getRemotefiles", method = RequestMethod.GET)
    public Map<String, Object> getRemoteFiles(String path, String userName, String extName) throws Exception {
        List<FileItem> files = new ArrayList<FileItem>();
        String code = null;
        String message = null;
        try {
            files = browserfileService.getRemotefile(path, userName, extName);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getRemotefiles", path, userName, extName));
            code = CloudConstants.FILE_GET_CODE;
            message = CloudConstants.FILE_GET_MSG;
        }
        return DataFormatUtils.format(code, message, files);
    }

    @RequestMapping(value = "/uploadfiles", method = RequestMethod.POST)
    public Map<String, Object> uploadFiles(UploadFileItem fileItem, String userName) throws Exception {
        String progress = null;
        String code = null;
        String message = null;
        try {
            progress = uploadFileService.uploadFile(fileItem, userName);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "uploadfiles", fileItem.toString(), userName));
            code = CloudConstants.FILE_UPLOAD_CODE;
            message = CloudConstants.FILE_UPLOAD_MSG;
        }
        return DataFormatUtils.format(code, message, progress);
    }

    @RequestMapping(value = "/mergeChunkfiles", method = RequestMethod.POST)
    public Map<String, Object> mergeChunkfiles(UploadFileItem fileItem, String userName) throws Exception {
        String progress = null;
        String code = null;
        String message = null;
        try {
            progress = uploadFileService.mergeChunkFile(fileItem, userName);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "mergeChunkfiles", fileItem.toString(), userName));
            code = CloudConstants.FILE_MERGE_CODE;
            message = CloudConstants.FILE_MERGE_MSG;
        }
        return DataFormatUtils.format(code, message, progress);
    }

    @RequestMapping(value = "/cleanUploadfiles", method = RequestMethod.POST)
    public Map<String, Object> cleanUploadfiles(UploadFileItem fileItem, String userName) throws Exception {
        String progress = null;
        String code = null;
        String message = null;
        try {
            progress = uploadFileService.cleanUploadFile(fileItem, userName);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "cleanUploadfiles", fileItem.toString(), userName));
            code = CloudConstants.FILE_MERGE_CODE;
            message = CloudConstants.FILE_MERGE_MSG;
        }
        return DataFormatUtils.format(code, message, progress);
    }

}
