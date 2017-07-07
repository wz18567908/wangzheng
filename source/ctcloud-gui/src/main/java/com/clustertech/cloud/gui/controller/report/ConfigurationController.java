package com.clustertech.cloud.gui.controller.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.controller.BaseController;
import com.clustertech.cloud.gui.domain.report.LicenseDateConfig;
import com.clustertech.cloud.gui.domain.report.ReportSetting;
import com.clustertech.cloud.gui.service.report.ConfigurationService;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.DataFormatUtils;
import com.clustertech.cloud.gui.utils.StringUtil;

@RestController
@RequestMapping("/api/report/config")
public class ConfigurationController extends BaseController<ConfigurationController> implements Serializable {
    private static final long serialVersionUID = -7046824579073779889L;

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(value = "/setting/list", method = RequestMethod.GET)
    public Map<String, Object> getReportSetting() {
        List<ReportSetting> resultList = new ArrayList<ReportSetting>();
        String message = "";
        try {
            resultList = configurationService.getReportSetting();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getReportSetting"));
            message = CloudConstants.REPORT_SETTING_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }

    @RequestMapping(value = "/license/date/add", method = RequestMethod.POST)
    public Map<String, Object> addLicenseDateConfig(String dateType, String startTime, String endTime) {
        String message = "";
        try {
//            Date formateStartTime = dateFormat.parse(startTime);
//            Date formateEndTime = dateFormat.parse(endTime);

            Map<String, Object> primaryKeyValueMap = new HashMap<String, Object>();
            primaryKeyValueMap.put("dateType", dateType);
            primaryKeyValueMap.put("startTime", startTime);
            primaryKeyValueMap.put("endTime", endTime);

            LicenseDateConfig licenseDateConfig = new LicenseDateConfig();
            licenseDateConfig.setDateType(dateType);
            licenseDateConfig.setStartTime(startTime);
            licenseDateConfig.setEndTime(endTime);

            configurationService.addLicenseDateConfig(licenseDateConfig, primaryKeyValueMap);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "addlicenseDateConfig", dateType, startTime, endTime));
            message = CloudConstants.REPORT_LICENSE_DATE_ADD_MSG;
        }
        return DataFormatUtils.format(message, "");
    }

    @RequestMapping(value = "/license/date/delete", method = RequestMethod.POST)
    public Map<String, Object> deleteLicenseDateConfig(String id) {
        String message = "";
        try {
            configurationService.deleteLicenseDateConfig(id);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "deleteLicenseDateConfig", id));
            message = CloudConstants.REPORT_LICENSE_DATE_DELETE_MSG;
        }
        return DataFormatUtils.format(message, "");
    }

    @RequestMapping(value = "/license/date/list", method = RequestMethod.GET)
    public Map<String, Object> getAllLicenseDateConfig() {
        List<LicenseDateConfig> resultList = new ArrayList<LicenseDateConfig>();
        String message = "";
        try {
            resultList = configurationService.getAllLicenseDateConfig();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getAllLicenseDateConfig"));
            message = CloudConstants.REPORT_LICENSE_CHART_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }
}
