package com.clustertech.cloud.gui.controller.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.controller.BaseController;
import com.clustertech.cloud.gui.service.report.ShareStorageUsageService;
import com.clustertech.cloud.gui.utils.DataFormatUtils;

@RestController
@RequestMapping("/api/report/wang")
public class ShareStorageController extends BaseController<ShareStorageController> implements Serializable {
    
    private static final long serialVersionUID = -4708702658800873158L;
    @Autowired
    private ShareStorageUsageService shareStorageUsageService;


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Map<String, Object> test() {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        String message = "";
        String storage = "/home/wang";
        shareStorageUsageService.setTimeUnitEnum("MONTH");
        shareStorageUsageService.setShareStorageEnum("USED");
        shareStorageUsageService.setStartTime("2017-06-01 00:00:00");
        shareStorageUsageService.setEndTime("2017-07-31 23:59:59");
            resultList = shareStorageUsageService.getShareStorageList(storage);
        return DataFormatUtils.format(message, resultList);
    }
}
