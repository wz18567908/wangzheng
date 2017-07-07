package com.clustertech.cloud.gui.service.job;

import java.util.List;
import java.util.ListIterator;

import org.springframework.stereotype.Service;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.upload.FileItem;
import com.clustertech.cloud.gui.upload.FileUtils;

@Service
public class BrowserFileService {

    public List<FileItem> getRemotefile(String path, String user, String extName) throws Exception {
        if (user == null) {
            throw new CTCloudException("invalidate user");
        }
        String command = "ls -lQn --time-style=full-iso '" + path + "/' | grep -v ^c|grep -v ^b";
        List<FileItem> items = FileUtils.utilList(user, path, command);
        if (items == null)
            return null;
        ListIterator<FileItem> it = items.listIterator();
        int num = 1;
        while (it.hasNext()) {
            FileItem item = it.next();
            if (extName != null) {
                String[] extNames = extName.split(";");
                boolean matched = false;
                for (String ext : extNames) {
                    if (FileUtils.isMatched(item.getName(), ext)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched && !item.isFolder()) {
                    it.remove();
                    num--;
                }
            }
            item.setId(num);
            num++;
        }
        return items;
    }

}
