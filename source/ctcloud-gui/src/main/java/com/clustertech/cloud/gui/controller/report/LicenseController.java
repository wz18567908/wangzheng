package com.clustertech.cloud.gui.controller.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.controller.BaseController;
import com.clustertech.cloud.gui.service.report.LicenseUsageService;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.DataFormatUtils;
import com.clustertech.cloud.gui.utils.StringUtil;

@RestController
@RequestMapping("/api/report/license")
public class LicenseController extends BaseController<LicenseController> implements Serializable {
    private static final long serialVersionUID = -1409342741416060688L;

    @Autowired
    private LicenseUsageService userLicenseService;

    @RequestMapping(value = "/date/range", method = RequestMethod.GET)
    public Map<String, Object> getDataRange() {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        String message = "";
        try {
            resultList = userLicenseService.getDateRange();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getDataRange"));
            message = CloudConstants.REPORT_LICENSE_FILTER_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }

    @RequestMapping(value = "/info/{filter}/list", method = RequestMethod.GET)
    public Map<String, Object> getQueryInfo(@PathVariable(value = "filter") String filter) {
        List<String> resultList = new ArrayList<String>();
        String message = "";
        try {
            userLicenseService.setReportFilterEnum(filter.toUpperCase());
            resultList = userLicenseService.getQueryInfo();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getQueryInfo", filter));
            message = CloudConstants.REPORT_LICENSE_FILTER_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }

    @RequestMapping(value = "/info/user/list", method = RequestMethod.GET)
    public Map<String, Object> getUserListByGroups(String groups) {
        List<String> resultList = new ArrayList<String>();
        String message = "";
        try {
            resultList = userLicenseService.getUserListByGroups(groups);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getUserListByGroups", groups));
            message = CloudConstants.REPORT_LICENSE_USER_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }

    @RequestMapping(value = "/info/feature/list", method = RequestMethod.GET)
    public Map<String, Object> getFeatureListByLicenses(String licenses) {
        List<String> resultList = new ArrayList<String>();
        String message = "";
        try {
            resultList = userLicenseService.getFeatureListByLicenses(licenses);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getFeatureListByLicenses", licenses));
            message = CloudConstants.REPORT_LICENSE_FEATURE_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }

    @RequestMapping(value = "/chart", method = RequestMethod.POST)
    public Map<String, Object> getLicenseUsageList(String timeUnit, String startTime, String endTime, String users,
            String groups, String licenses, String features, String licenseTimeType, String resourceType) {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        String message = "";
        try {
            userLicenseService.setUsers(users);
            userLicenseService.setGroups(groups);
            userLicenseService.setLicenses(licenses);
            userLicenseService.setFeatures(features);
            userLicenseService.setTimeUnitEnum(timeUnit);
            userLicenseService.setStartTime(startTime);
            userLicenseService.setEndTime(endTime);
            userLicenseService.setResourceEnum(resourceType);
            userLicenseService.setLicenseTimeEnum(licenseTimeType);
            resultList = userLicenseService.getUserLicenseUsageList(users, groups, licenses, features);
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getLicenseUsageList", timeUnit, startTime, endTime,
                    users, groups, licenses, features, licenseTimeType, resourceType));
            message = CloudConstants.REPORT_LICENSE_FILTER_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }
    
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Map<String, Object> test() {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        String message = "";
        String startTime = "2017-06-01 00:00:00";
        String endTime = "2017-07-31 23:59:59";
        try {
            resultList = userLicenseService.test();
        } catch (Throwable th) {
            logger.error(StringUtil.formatErrorLogger(th, "getDataRange"));
            message = CloudConstants.REPORT_LICENSE_FILTER_LIST_MSG;
        }
        return DataFormatUtils.format(message, resultList);
    }
}
