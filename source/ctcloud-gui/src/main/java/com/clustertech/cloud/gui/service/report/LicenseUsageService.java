package com.clustertech.cloud.gui.service.report;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clustertech.cloud.gui.dao.report.LicenseDateConfigDao;
import com.clustertech.cloud.gui.dao.report.LicenseUsageDao;
import com.clustertech.cloud.gui.dao.report.ShareStorageUsageDao;
import com.clustertech.cloud.gui.dao.report.UserLicenseUsageDao;
import com.clustertech.cloud.gui.domain.report.LicenseDateConfig;
import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.utils.CloudConstants.LicenseConfigDateEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.LicenseTimeEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.ResourceEnum;
import com.clustertech.cloud.gui.utils.StringUtil;

@Service
@Transactional
public class LicenseUsageService extends ReportBaseService {
    public static final Logger logger = Logger.getLogger(LicenseUsageService.class);

    private ThreadLocal<LicenseTimeEnum> licenseTimeEnum = new ThreadLocal<LicenseTimeEnum>();
    private ThreadLocal<LicenseConfigDateEnum> licenseConfigDateEnum = new ThreadLocal<LicenseConfigDateEnum>();

    private enum TimeLengthType {
        FIRST_DAY,
        LAST_DAY
    }

    @Autowired
    private LicenseDateConfigDao licenseDateConfigDao;
    @Autowired
    private UserLicenseUsageDao userLicenseUsageDao;
    @Autowired
    private LicenseUsageDao licenseUsageDao;
    @Autowired
    private ShareStorageUsageDao shareStorageUsageDao;

    public List<Map<String, Object>> getDateRange() {
        return userLicenseUsageDao.getDateRange();
    }
    
    public List<Map<String, Object>> test() {
        return shareStorageUsageDao.getShareStorageUsageList("%Y/%m", "2017-06-01 00:00:00",
                "2017-07-31 23:59:59");
    }

    public List<String> getQueryInfo() throws CTCloudException {
        String propertyName = "";
        switch(reportFilterEnum.get()) {
        case USER:
            propertyName = "userName";
            break;
        case GROUP:
            propertyName = "groupName";
            break;
        case LICENSE:
            propertyName = "licenseName";
            break;
        case FEATURE:
            propertyName = "featureName";
            break;
        default:
            break;
        }
        if (propertyName.isEmpty()) {
            throw new CTCloudException(String.format("The reportFilter[%s] is not existence in the license report.",
                    reportFilterEnum.get().name()));
        }
        return userLicenseUsageDao.getDistinctProperty("UserLicense", propertyName);
    }

    public List<String> getUserListByGroups(String groups) throws CTCloudException {
        if (groups.isEmpty()) {
            return userLicenseUsageDao.getDistinctProperty("UserLicense", "userName");
        } else {
            return userLicenseUsageDao.getDistinctPropertyByInClause("UserLicense", "userName", "groupName",
                    StringUtil.formateSqlInData(groups));
        }
    }

    public List<String> getFeatureListByLicenses(String filterValue) throws CTCloudException {
        if (filterValue.isEmpty()) {
            return userLicenseUsageDao.getDistinctProperty("UserLicense", "featureName");
        } else {
            return userLicenseUsageDao.getDistinctPropertyByInClause("UserLicense", "featureName", "licenseName",
                    StringUtil.formateSqlInData(filterValue));
        }
    }

    public List<Map<String, Object>> getUserLicenseUsageList(String users, String groups, String licenses, String features) {
        List<Map<String, Object>> userLicenseUsageList = userLicenseUsageDao.getUserLicenseUsageList(HQL_TIME_FORMAT,
                getStartTime(), getEndTime(), getHqlWhereClause1(users, groups, licenses, features));
        if (ResourceEnum.FEATURE_PERIOD == resourceEnum.get()) {
            return processUserLicenseResult(userLicenseUsageList);
        } else {
            return removeUnnecessaryData(getLicenseUsageList(users, groups, licenses, features));
        }
    }

    public List<Map<String, Object>> getUserLicenseUsageDetail(String users, String groups, String licenses, String features) {
        List<Map<String, Object>> licenseUsageList = new ArrayList<Map<String, Object>>();

        try {
            if (ResourceEnum.FEATURE_PERIOD == resourceEnum.get()) {
                 licenseUsageList = userLicenseUsageDao.getUserLicenseUsageDetail(licenseTimeEnum.get(),
                        HQL_TIME_FORMAT, getStartTime(), getEndTime(), getHqlWhereClause());
                if (LicenseTimeEnum.WORK_TIME == licenseTimeEnum.get()) {
                    licenseUsageList = processDetailSQLResult(licenseUsageList);
                }
            } else {
                licenseUsageList = processUserLicensePeakAndUtResult(users, groups, licenses, features);
            }
        } catch (ParseException e) {
            Logger logger = Logger.getLogger("LicenseUsageDao.getUserLicenseUsageDetail()");
            logger.error(e.getMessage());
        }

        return licenseUsageList;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> processUserLicensePeakAndUtResult(String users, String groups, String licenses, String features) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        if (ResourceEnum.FEATURE_PERIOD == resourceEnum.get()) {
            List<Map<String, Object>> objList;
            objList = getLicenseUsageList(users, groups, licenses, features);
            if (objList != null) {
                for (int i = 0; i < objList.size(); i++) {
                    dataList.addAll((List<Map<String, Object>>) objList.get(i).get("data"));
                }
            }
            for (Map<String, Object> data : dataList) {
                String licenseName = data.get("label").toString().split(":")[0];
                String featurename = data.get("label").toString().split(":")[1];
                List<List<String>> elementList = (List<List<String>>) data.get("data");
                for (List<String> element : elementList) {
                    if (!element.get(1).equals("0.00")) {
                        Map<String, Object> peakAndUtResultMap = new HashMap<String, Object>();
                        DecimalFormat df = new DecimalFormat("#");
                        peakAndUtResultMap.put("TIME", element.get(0));
                        peakAndUtResultMap.put("LICENSE_FILE", licenseName);
                        peakAndUtResultMap.put("FEATURE", featurename);
                        peakAndUtResultMap.put("MAX_TOTAL", element.get(2));
                        peakAndUtResultMap.put("AVG_IN_USE", element.get(3));
                        if (ResourceEnum.FEATURE_PEAK == resourceEnum.get()) {
                            peakAndUtResultMap.put("RESOURCE",
                                    df.format(Double.parseDouble(element.get(1).toString())));
                        } else {
                            peakAndUtResultMap.put("RESOURCE", element.get(1));
                        }

                        resultList.add(peakAndUtResultMap);
                    }
                }
            }

        }
        return resultList;
    }

    public List<Map<String, Object>> getLicenseUsageList(String users, String groups, String licenses, String features) {
        return processSQLResult(licenseUsageDao.getLicenseUsageList(getUnitTimeFormat(), getStartTime(),
                getEndTime(), getHqlWhereClause1(users, groups, licenses, features)));
    }

    private List<Map<String, Object>> processDetailSQLResult(List<Map<String, Object>> userLicenseUsageList)
            throws ParseException {
        for (Map<String, Object> resultMap : userLicenseUsageList) {
            String startTimeString = (String) resultMap.get("START_TIME");
            String endTimeString = (String) resultMap.get("END_TIME");
            long totallTimeSeconds = getWorkTimeLength(startTimeString, endTimeString);
            DecimalFormat df = new DecimalFormat("#");
            resultMap.put("TOTALL_TIME_LENGTH", df.format(converterSecondsToMinute(totallTimeSeconds)));
        }
        return userLicenseUsageList;
    }

    private List<Map<String, Object>> processSQLResult(List<Map<String, Object>> licenseUsageList) {

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        List<String> licenseNameList = new ArrayList<String>();
        for (Map<String, Object> obj : licenseUsageList) {
            String licenseName = obj.get("licenseName").toString();
            if (licenseNameList.contains(licenseName)) {
                continue;
            }
            licenseNameList.add(licenseName);
        }

        Map<String, List<String>> licenseFeatureMap = new HashMap<String, List<String>>();
        for (String licenseName : licenseNameList) {
            List<String> featureList = new ArrayList<String>();
            for (Map<String, Object> obj : licenseUsageList) {
                String feature = obj.get("featureName").toString();
                if (!licenseName.equals(obj.get("licenseName").toString()) || featureList.contains(feature)) {
                    continue;
                }
                featureList.add(feature);
            }
            licenseFeatureMap.put(licenseName, featureList);
        }

        for (String licenseName : licenseNameList) {
            Map<String, Object> elementBasedObj = new HashMap<String, Object>();
            elementBasedObj.put("element", licenseName);
            List<Map<String, Object>> elementData = new ArrayList<Map<String, Object>>();
            for (String featureName : licenseFeatureMap.get(licenseName)) {
                elementData.add(getFeatureUsage(licenseName, featureName, licenseUsageList));
            }
            elementBasedObj.put("data", elementData);
            result.add(elementBasedObj);
        }
        return result;
    }

    private Map<String, Object> getFeatureUsage(String license, String feature,
            List<Map<String, Object>> licenseUsageList) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("label", license + ":" + feature);

        List<Object> data = new ArrayList<Object>();

        SimpleDateFormat sf = getDateFormat();
        Calendar startCalendar = getStartCalendar();
        Calendar endCalendar = getEndCalendar();

        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.clear();
        tempCalendar.setTime(startCalendar.getTime());

        String currentDateTime = null;
        while (tempCalendar.before(endCalendar)) {
            currentDateTime = sf.format(tempCalendar.getTime());
            List<String> dataContent = new ArrayList<String>();
            dataContent.add(currentDateTime);
            addUnitToCalendar(tempCalendar);
            double resource = 0;
            int maxTotalNum = 0;
            int tmpNum = 0;
            double avgInuseNum = 0;
            int count = 0;
            for (Map<String, Object> obj : licenseUsageList) {
                String licenseName = obj.get("licenseName").toString();
                String featureName = obj.get("featureName").toString();
                int total = Integer.parseInt(obj.get("total").toString());
                int inUseNum = Integer.parseInt(obj.get("inUse").toString());
                if (!licenseName.equals(license) || !featureName.equals(feature)) {
                    continue;
                }

                String time = obj.get("time").toString();
                if (currentDateTime.equals(time)) {
                    if (total > maxTotalNum) {
                        maxTotalNum = total;
                    }
                    count += 1;
                    tmpNum += inUseNum;
                    if (ResourceEnum.FEATURE_PEAK == resourceEnum.get()) {
                        if (inUseNum > resource) {
                            resource = inUseNum;
                        }
                    }
                }
            }
            if (count != 0) {
                avgInuseNum = (double) tmpNum / count;
            } else {
                avgInuseNum = 0.00;
            }
            if (ResourceEnum.FEATURE_USAGE == resourceEnum.get()) {
                if (count == 0) {
                    resource = 0.00;
                } else {
                    resource = avgInuseNum / maxTotalNum * 100;
                }
            }
            DecimalFormat df = new DecimalFormat("#0.00");
            dataContent.add(String.valueOf(df.format(resource)));
            dataContent.add(String.valueOf(maxTotalNum));
            if (!String.valueOf(avgInuseNum).equals("0.00")) {
                dataContent.add(String.valueOf(Math.round(avgInuseNum)));
            } else {
                dataContent.add("0.00");
            }
            if (dataContent.size() < 2) {
                dataContent.add("0");
            }
            data.add(dataContent);
        }

        result.put("data", data);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> removeUnnecessaryData(List<Map<String, Object>> objList) {
        for (Map<String, Object> obj : objList) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) obj.get("data");
            for (Map<String, Object> elementData : data) {
                List<List<String>> dataList = (List<List<String>>) elementData.get("data");
                for (List<String> dataString : dataList) {
                    Iterator<String> it = dataString.iterator();
                    while (it.hasNext()) {
                        String item = it.next();
                        if (item.equals(dataString.get(2)) || item.equals(dataString.get(3))) {
                            it.remove();// remove the current item
                        }

                    }
                }
            }
        }
        return objList;
    }

    private List<Date> getTimeDataByDateType(LicenseConfigDateEnum type) throws ParseException {
        List<Date> resultList = new ArrayList<Date>();
        List<LicenseDateConfig> licenseDateConfigList = licenseDateConfigDao.getTimeDataByDateType(type);
        for (LicenseDateConfig licenseDateConfig : licenseDateConfigList) {
            String startTime = licenseDateConfig.getStartTime().toString();
            String endTime = licenseDateConfig.getEndTime().toString();
            resultList.addAll(getDates(startTime, endTime, type));
        }
        return resultList;
    }

    private List<Date> getWorkDaysData(String startTime, String endTime) throws ParseException {
        Date startDate = dateFormatYear.parse(startTime);
        Date endDate = dateFormatYear.parse(endTime);
        List<Date> workDays = getDates(startTime, endTime, LicenseConfigDateEnum.WORKING);
        List<Date> holidayDays = getTimeDataByDateType(LicenseConfigDateEnum.VACATION);
        List<Date> exchangDayDays = getTimeDataByDateType(LicenseConfigDateEnum.OVERTIME);

        if (holidayDays.size() > 0) {
            holidayDays.retainAll(workDays);
            workDays.removeAll(holidayDays);
        }
        if (exchangDayDays.size() > 0) {
            List<Date> exchangDays = new ArrayList<Date>();
            for (Date exchangDay : exchangDayDays) {
                if (exchangDay.compareTo(endDate) <= 0 && exchangDay.compareTo(startDate) >= 0) {
                    exchangDays.add(exchangDay);
                }
            }
            workDays.addAll(exchangDays);
        }
        Collections.sort(workDays);
        return workDays;
    }

    private List<Map<String, Object>> processUserLicenseResult(List<Map<String, Object>> userLicenseUsageList) {
        List<String> licenseFileList = new ArrayList<String>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> userLicenseMap : userLicenseUsageList) {
            String licenseFile = (String) userLicenseMap.get("licenseName");
            if (licenseFileList.contains(licenseFile)) {
                continue;
            }
            licenseFileList.add(licenseFile);
        }
        for (String licenseFile : licenseFileList) {
            Map<String, Object> elementObj = new HashMap<String, Object>();
            List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
            elementObj.put("element", licenseFile);
            dataList = getFeatureDataByLicense(licenseFile, userLicenseUsageList);
            elementObj.put("data", mergedDuplicatedLabel(dataList));
            resultList.add(elementObj);
        }
        return resultList;
    }

    private List<Map<String, Object>> mergedDuplicatedLabel(List<Map<String, Object>> elementData) {
        List<Map<String, Object>> mergedObj = new ArrayList<Map<String, Object>>();
        List<String> lables = new ArrayList<String>();
        List<String> dataTimes = new ArrayList<String>();
        for (Map<String, Object> element : elementData) {
            String label = element.get("label").toString();
            if (!lables.contains(label)) {
                lables.add(label);
            }
        }

        if (!elementData.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<List<String>> dataList = (List<List<String>>) elementData.get(0).get("data");
            for (List<String> data : dataList) {
                String dateValue = data.get(0);
                if (!dataTimes.contains(dateValue)) {
                    dataTimes.add(dateValue);
                }
            }
        }

        for (String perLable : lables) {
            Map<String, Object> featureUsageMap = new HashMap<String, Object>();
            featureUsageMap.put("label", perLable);
            List<Object> timeData = new ArrayList<Object>();
            for (String perTime : dataTimes) {
                List<Object> mapData = new ArrayList<Object>();
                mapData.add(perTime);
                mapData.add(getTimeLength(elementData, perLable, perTime));
                timeData.add(mapData);
            }
            featureUsageMap.put("data", timeData);
            mergedObj.add(featureUsageMap);
        }
        return mergedObj;
    }

    private double getTimeLength(List<Map<String, Object>> elementData, String perLable, String perTime) {
        Double timeLength = 0.0;
        for (Map<String, Object> element : elementData) {
            if (perLable.equals(element.get("label").toString())) {
                @SuppressWarnings("unchecked")
                List<List<String>> dataLists = (List<List<String>>) element.get("data");
                for (List<String> data : dataLists) {
                    String dateValue = data.get(0);
                    if (dateValue.equals(perTime)) {
                        timeLength = timeLength + Double.valueOf(String.valueOf(data.get(1)));
                    }
                }
            }
        }
        return timeLength;
    }

    private List<Map<String, Object>> getFeatureDataByLicense(String licenseFile,
            List<Map<String, Object>> userLicenseUsageList) {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> userLicenseMap : userLicenseUsageList) {
            Map<String, Object> elementDataMap = new HashMap<String, Object>();
            String licenseFileName = (String) userLicenseMap.get("licenseName");
            String featureName = (String) userLicenseMap.get("featureName");
            String startTime = (String) userLicenseMap.get("startTime");
            String endTime = (String) userLicenseMap.get("endTime");
            if (licenseFile.equals(licenseFileName)) {
                elementDataMap.put("label", licenseFileName + ":" + featureName);
                List<Object> data = getTimeData(startTime, endTime);
                elementDataMap.put("data", data);
                resultList.add(elementDataMap);
            }
        }
        return resultList;
    }

    private List<Object> getTimeData(String startTime, String endTime) {
        List<Object> result = new ArrayList<Object>();

        Calendar startCalendar = getStartCalendar();
        Calendar endCalendar = getEndCalendar();

        Calendar tempStartCalendar = Calendar.getInstance();
        tempStartCalendar.clear();
        tempStartCalendar.setTime(startCalendar.getTime());

        Calendar tempEndCalendar = Calendar.getInstance();
        tempEndCalendar.clear();
        tempEndCalendar.setTime(startCalendar.getTime());

        String currentDateTime = null;
        SimpleDateFormat sf = getDateFormat();
        while (tempEndCalendar.before(endCalendar)) {
            Date startDate = new Date();
            Date endDate = new Date();
            List<Object> data = new ArrayList<Object>();
            currentDateTime = sf.format(tempEndCalendar.getTime());
            data.add(currentDateTime);
            addUnitToCalendar(tempEndCalendar);

            Calendar sCalendar = Calendar.getInstance();
            sCalendar.clear();
            Calendar eCalendar = Calendar.getInstance();
            eCalendar.clear();
            try {
                sCalendar.setTime(dateFormat.parse(startTime));
                eCalendar.setTime(dateFormat.parse(endTime));
            } catch (ParseException e) {
                // data corruption; ignore
                continue;
            }

            boolean hasTimeLength = true;
            if (tempStartCalendar.before(sCalendar)) {
                if (tempEndCalendar.before(sCalendar)) {
                    hasTimeLength = false;
                } else {
                    if (tempEndCalendar.before(eCalendar)) {
                        startDate = sCalendar.getTime();
                        endDate = tempEndCalendar.getTime();
                    } else {
                        startDate = sCalendar.getTime();
                        endDate = eCalendar.getTime();
                    }
                }
            } else {
                if (tempStartCalendar.after(eCalendar)) {
                    hasTimeLength = false;
                } else {
                    if (tempEndCalendar.before(eCalendar)) {
                        startDate = tempStartCalendar.getTime();
                        endDate = tempEndCalendar.getTime();
                    } else {
                        startDate = tempStartCalendar.getTime();
                        endDate = eCalendar.getTime();
                    }
                }
            }

            long timeLength;
            if (hasTimeLength) {
                String sTime = dateFormat.format(startDate);
                String eTime = dateFormat.format(endDate);
                try {
                    if (LicenseTimeEnum.TOTAL_TIME == licenseTimeEnum.get()) {
                        timeLength = getTotallTimeLength(sTime, eTime);
                    } else {
                        timeLength = getWorkTimeLength(sTime, eTime);
                    }
                } catch (ParseException e) {
                    // data corruption; ignore
                    continue;
                }
            } else {
                timeLength = 0;
            }

            if (LicenseTimeEnum.TOTAL_TIME == licenseTimeEnum.get()) {
                data.add(timeLength);
            } else {
                data.add(converterSecondsToMinute(timeLength));
            }
            data.add(timeLength);
            result.add(data);
            tempStartCalendar.clear();
            tempStartCalendar.setTime(tempEndCalendar.getTime());
        }
        return result;
    }

    private double converterSecondsToMinute(long seconds) {
        double resultOfMinute = (double) seconds / 60;
        BigDecimal bigDecimal = new BigDecimal(resultOfMinute);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private long getTotallTimeLength(String startTime, String endTime) throws ParseException {
        List<String> timeLength = getMinuteTime(startTime, endTime);
//        List<String> timeLength = licenseDateConfigDao.getMinuteTimeLength(startTime, endTime);
        long timeValue = 0;
        for (String tmp : timeLength) {
            timeValue = Long.valueOf(tmp).longValue();
        }
        return timeValue;
    }

    private long getWorkTimeLength(String startTime, String endTime) throws ParseException {
        Date startDate = dateFormatYear.parse(startTime);
        Date endDate = dateFormatYear.parse(endTime);
        long totallTimeLenght = 0;
        boolean startTimeValidate = false;
        boolean endTimeValidate = false;

        List<Date> workDayList = getWorkDaysData(startTime, endTime);
        for (Date workDay : workDayList) {
            if (workDay.compareTo(startDate) == 0) {
                startTimeValidate = true;
            }
            if (workDay.compareTo(endDate) == 0) {
                endTimeValidate = true;
            }
        }

        List<LicenseDateConfig> workTimeList = licenseDateConfigDao.getWorkTimeData();
        long workTimeLength = 0;
        for (LicenseDateConfig workTime : workTimeList) {
            workTimeLength += getTimeDiffSeconds(workTime.getStartTime().toString(),
                    workTime.getEndTime().toString());
        }

        if (workDayList.size() == 1 && startTimeValidate && endTimeValidate) {
            totallTimeLenght = getWorkTimeLengthForOneWorkDay(startTime, endTime);
        } else if (workDayList.size() >= 2 && startTimeValidate && endTimeValidate) {
            Map<String, Long> result = getWorkTimeLengthForMoreWorkDay(startTime, endTime);
            totallTimeLenght = result.get(TimeLengthType.FIRST_DAY.name())
                    + result.get(TimeLengthType.LAST_DAY.name());
            if (workDayList.size() > 2) {
                totallTimeLenght += (workDayList.size() - 2) * workTimeLength;
            }
        } else if (!(startTimeValidate && endTimeValidate)) {
            Map<String, Long> result = getWorkTimeLengthForMoreWorkDay(startTime, endTime);
            if (startTimeValidate) {
                totallTimeLenght = result.get(TimeLengthType.FIRST_DAY.name());
            }
            if (endTimeValidate) {
                totallTimeLenght += result.get(TimeLengthType.LAST_DAY.name());
            }
            if (workDayList.size() >= 2) {
                if (!(startTimeValidate || endTimeValidate)) {
                    totallTimeLenght += workDayList.size() * workTimeLength;
                } else {
                    totallTimeLenght += (workDayList.size() - 1) * workTimeLength;
                }
            }
        } else {
            totallTimeLenght = 0;
        }
        return totallTimeLenght;
    }

    private long getWorkTimeLengthForOneWorkDay(String startTime, String endTime) throws ParseException {
        Date startResult = new Date();
        Date endResult = new Date();
        long result = 0;
        Date startDate = dateFormat.parse(startTime);
        Date endDate = dateFormat.parse(endTime);
        List<LicenseDateConfig> workTimeList = licenseDateConfigDao.getWorkTimeData();

        for (LicenseDateConfig workTime : workTimeList) {
            Date workStartDate = dateFormat
                    .parse(dateFormatYear.format(startDate) + " " + workTime.getStartTime());
            Date workEndDate = dateFormat
                    .parse(dateFormatYear.format(endDate) + " " + workTime.getEndTime());

            if (startDate.compareTo(workStartDate) <= 0 && endDate.compareTo(workStartDate) >= 0) {
                startResult = workStartDate;
                if (endDate.compareTo(workEndDate) <= 0) {
                    endResult = endDate;
                } else if (endDate.compareTo(workEndDate) > 0) {
                    endResult = workEndDate;
                }
            } else if (startDate.compareTo(workStartDate) > 0 && startDate.compareTo(workEndDate) < 0) {
                startResult = startDate;
                if (endDate.compareTo(workEndDate) <= 0) {
                    endResult = endDate;
                } else if (endDate.compareTo(workEndDate) > 0) {
                    endResult = workEndDate;
                }
            } else {
                startResult = endResult;
            }

            result += getTimeDiffSeconds(dateFormatHour.format(startResult), dateFormatHour.format(endResult));
        }
        return result;
    }

    private Map<String, Long> getWorkTimeLengthForMoreWorkDay(String startTime, String endTime) throws ParseException {
        Date startDate = dateFormat.parse(startTime);
        Date endDate = dateFormat.parse(endTime);
        long resultBefore = 0;
        long resultAfter = 0;
        List<LicenseDateConfig> workTimeList = licenseDateConfigDao.getWorkTimeData();
        Map<String, Long> resultMap = new HashMap<String, Long>();

        for (LicenseDateConfig workTime : workTimeList) {
            Date workStartDateBefore = dateFormat
                    .parse(dateFormatYear.format(startDate) + " " + workTime.getStartTime());
            Date workEndDateBefore = dateFormat
                    .parse(dateFormatYear.format(startDate) + " " + workTime.getEndTime());

            // Compute the first day of effective working time.
            Date startResultBefore = new Date();
            Date endResultBefore = new Date();
            if (startDate.compareTo(workStartDateBefore) < 0) {
                startResultBefore = workStartDateBefore;
                endResultBefore = workEndDateBefore;
            } else if (startDate.compareTo(workStartDateBefore) >= 0 && startDate.compareTo(workEndDateBefore) <= 0) {
                startResultBefore = startDate;
                endResultBefore = workEndDateBefore;
            } else {
                startResultBefore = endResultBefore;
            }
            resultBefore += getTimeDiffSeconds(dateFormatHour.format(startResultBefore),
                    dateFormatHour.format(endResultBefore));

            // Compute the last day of effective working time.
            Date startResultAfter = new Date();
            Date endResultAfter = new Date();
            Date workStartDateAfter = dateFormat
                    .parse(dateFormatYear.format(endDate) + " " + workTime.getStartTime());
            Date workEndDateAfter = dateFormat
                    .parse(dateFormatYear.format(endDate) + " " + workTime.getEndTime());

            if (endDate.compareTo(workStartDateAfter) >= 0 && endDate.compareTo(workEndDateAfter) <= 0) {
                startResultAfter = workStartDateAfter;
                endResultAfter = endDate;
            } else if (endDate.compareTo(workEndDateAfter) > 0) {
                startResultAfter = workStartDateAfter;
                endResultAfter = workEndDateAfter;
            } else {
                startResultAfter = endResultAfter;
            }
            resultAfter += getTimeDiffSeconds(dateFormatHour.format(startResultAfter),
                    dateFormatHour.format(endResultAfter));
        }
        resultMap.put(TimeLengthType.FIRST_DAY.name(), resultBefore);
        resultMap.put(TimeLengthType.LAST_DAY.name(), resultAfter);
        return resultMap;
    }

    private List<Date> getDates(String startTime, String endTime, LicenseConfigDateEnum configDate) throws ParseException {
        List<Date> dateList = new ArrayList<Date>();

        Calendar startCal = Calendar.getInstance();
        Date startDate = dateFormatYear.parse(startTime);
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        Date endDate = dateFormatYear.parse(endTime);
        endCal.setTime(endDate);

        while (endDate.compareTo(startCal.getTime()) >= 0) {
            int day = startCal.get(Calendar.DAY_OF_WEEK);
            switch (configDate) {
            case WORKING:
            case VACATION:
                if (!(day == Calendar.SUNDAY || day == Calendar.SATURDAY)) {
                    dateList.add(startCal.getTime());
                }
                break;
            case OVERTIME:
                dateList.add(startCal.getTime());
                break;
            default:
                break;
            }
            startCal.add(Calendar.DATE, 1);
        }
        return dateList;
    }

    private long getTimeDiffSeconds(String startTime, String endTime) throws ParseException {
        Date startDate = dateFormatHour.parse(startTime);
        Date endDate = dateFormatHour.parse(endTime);
        return (endDate.getTime() - startDate.getTime()) / 1000;
    }
    
    private List<String> getMinuteTime(String startTime, String endTime) throws ParseException {
        Date startDate = dateFormat.parse(startTime);
        Date endDate = dateFormat.parse(endTime);
        List<String> result = new ArrayList<String>();
        long minutes = (endDate.getTime() - startDate.getTime()) / 1000 / 60;
        result.add(String.valueOf(minutes));
        return result;
    }

    public void setLicenseTimeEnum(String licenseTimeEnum) {
        this.licenseTimeEnum.set(LicenseTimeEnum.valueOf(licenseTimeEnum));
    }

    public void setLicenseConfigDateEnum(String licenseConfigDateEnum) {
        this.licenseConfigDateEnum.set(LicenseConfigDateEnum.valueOf(licenseConfigDateEnum));
    }
}
