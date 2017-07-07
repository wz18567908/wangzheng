package com.clustertech.cloud.gui.service.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustertech.cloud.gui.utils.CloudConstants.ReportFilterEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.ResourceEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.ShareStorageEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.TimeUnitEnum;
import com.clustertech.cloud.gui.utils.StringUtil;

public class ReportBaseService {
    protected static final String HQL_TIME_FORMAT = "%Y-%m-%d %H:%i:%S";
    protected static final SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH:mm:ss");
    protected static final SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected final String DEFAULT_VALUE="0";

    protected ThreadLocal<String> startTime = new ThreadLocal<String>();
    protected ThreadLocal<String> endTime = new ThreadLocal<String>();
    protected ThreadLocal<TimeUnitEnum> timeUnitEnum  = new ThreadLocal<TimeUnitEnum>();
    protected ThreadLocal<ReportFilterEnum> reportFilterEnum = new ThreadLocal<ReportFilterEnum>();
    protected ThreadLocal<ResourceEnum> resourceEnum = new ThreadLocal<ResourceEnum>();
    protected ThreadLocal<ShareStorageEnum> shareStorageEnum = new ThreadLocal<ShareStorageEnum>();

    private ThreadLocal<String[]> users = new ThreadLocal<String[]>();
    private ThreadLocal<String[]> groups = new ThreadLocal<String[]>();
    private ThreadLocal<String[]> projects = new ThreadLocal<String[]>();
    private ThreadLocal<String[]> licenses = new ThreadLocal<String[]>();
    private ThreadLocal<String[]> features = new ThreadLocal<String[]>();
    private ThreadLocal<String[]> applications = new ThreadLocal<String[]>();

    private ThreadLocal<Calendar> startCalendar = new ThreadLocal<>();
    private ThreadLocal<Calendar> endCalendar = new ThreadLocal<>();
    private ThreadLocal<String> timeFormat = new ThreadLocal<>();

    protected ReportBaseService() {
        users.set(new String[0]);
        groups.set(new String[0]);
        projects.set(new String[0]);
        licenses.set(new String[0]);
        features.set(new String[0]);
        applications.set(new String[0]);
    }

    protected void initCalendar(Calendar calendar, String dateString) {
        // dateString="2014-05-01 00:00:00"
        calendar.clear();
        calendar.set(Integer.parseInt(dateString.substring(0, 4)),
                Integer.parseInt(dateString.substring(5, 7)) - 1,
                Integer.parseInt(dateString.substring(8, 10)),
                Integer.parseInt(dateString.substring(11, 13)),
                Integer.parseInt(dateString.substring(14, 16)));
    }

    protected SimpleDateFormat getDateFormat() {
        switch (timeUnitEnum.get()) {
        case MONTH:
            return new SimpleDateFormat("yyyy/MM");
        case DAY:
            return new SimpleDateFormat("MM/dd");
        case HOUR:
            return new SimpleDateFormat("dd-HH:00");
        default:
            // Never get here.
            return null;
        }
    }

    protected void addUnitToCalendar(Calendar calendar) {
        switch (timeUnitEnum.get()) {
        case MONTH:
            calendar.add(Calendar.MONTH, 1);
            break;
        case DAY:
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            break;
        case HOUR:
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            break;
        default:
            // Never get here.
            break;
        }
    }

    public String getUnitTimeFormat() {
        switch (timeUnitEnum.get()) {
        case MONTH:
            timeFormat.set("%Y/%m");
            break;
        case DAY:
            timeFormat.set("%m/%d");
            break;
        case HOUR:
            timeFormat.set("%d-%H:00");
            break;
        default:
            // Never get here.
            break;
        }
        return timeFormat.get();
    }

    public String getHqlWhereClause() {
        StringBuffer buffer = new StringBuffer();
        if (getLicenses().length > 0) {
            buffer.append(" and licenseName in (" + StringUtil.formateSqlInData(getLicenses()) + ")");
        }
        if (getFeatures().length > 0) {
            buffer.append(" and featureName in (" + StringUtil.formateSqlInData(getFeatures()) + ")");
        }
        if (getUsers().length > 0) {
            buffer.append(" and userName in (" + StringUtil.formateSqlInData(getUsers()) + ")");
        }
        if (getGroups().length > 0) {
            buffer.append(" and groupName in (" + StringUtil.formateSqlInData(getGroups()) + ")");
        }
//        if (getProjects().length > 0) {
//            buffer.append(" and projectName in (" + StringUtil.formateSqlInData(getProjects()) + ")");
//        }
//        if (getApplications().length > 0) {
//            buffer.append(" and appName in (" + StringUtil.formateSqlInData(getApplications()) + ")");
//        }
        return buffer.toString();
    }
    
    public String getHqlWhereClause1(String users, String groups, String licenses, String features) {
        StringBuffer buffer = new StringBuffer();
        if (licenses.isEmpty()) {
            buffer.append("");
        } else {
            buffer.append(" and licenseName in (" + StringUtil.formateSqlInData(getLicenses()) + ")");
        }
        if (features.isEmpty()) {
            buffer.append("");
        } else {
            buffer.append(" and featureName in (" + StringUtil.formateSqlInData(getFeatures()) + ")");
        }
        if (users.isEmpty()) {
            buffer.append("");
        } else {
            buffer.append(" and userName in (" + StringUtil.formateSqlInData(getUsers()) + ")");
        }
        if (groups.isEmpty()) {
            buffer.append("");
        } else {
            buffer.append(" and groupName in (" + StringUtil.formateSqlInData(getGroups()) + ")");
        }
//        if (getProjects().length > 0) {
//            buffer.append(" and projectName in (" + StringUtil.formateSqlInData(getProjects()) + ")");
//        }
//        if (getApplications().length > 0) {
//            buffer.append(" and appName in (" + StringUtil.formateSqlInData(getApplications()) + ")");
//        }
        return buffer.toString();
    }

    public Calendar getStartCalendar() {
        startCalendar.set(Calendar.getInstance());
        initCalendar(startCalendar.get(), startTime.get());
        return startCalendar.get();
    }

    public Calendar getEndCalendar() {
        endCalendar.set(Calendar.getInstance());
        initCalendar(endCalendar.get(), endTime.get());
        return endCalendar.get();
    }
    
    protected String formatList(List<String> elements) {
        String result = "";

        if (elements.isEmpty()) {
            return result;
        }

        result += "'" + elements.get(0).trim() + "'";
        for (int i=1; i<elements.size(); i++) {
            result += ",'" + elements.get(i).trim() + "'";
        }
        return result;
    }
    
    protected void add1UnitToCalendar(Calendar calendar) {
        switch(timeUnitEnum.get()) {
            case MONTH :
                calendar.add(Calendar.MONTH, 1);
                break;
            case DAY :
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case HOUR :
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                break;
            default :
                //Never get here.
                break;
        }
    }
    
    public Map<String, Object> getJobLimitMap(List<Map<String, Object>> objList, String key, String label) {
        List<List<String>> dataList = new ArrayList<List<String>>();
        String currentDateTime = null;
        SimpleDateFormat sf = getDateFormat();
        Calendar startCalendar = getStartCalendar();
        Calendar endCalendar = getEndCalendar();
        Map<String, Object> jobMap = new HashMap<String, Object>();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.clear();
        tempCalendar.setTime(startCalendar.getTime());
        while (tempCalendar.before(endCalendar)) {
            List<String> data = new ArrayList<String>();
            currentDateTime = sf.format(tempCalendar.getTime());
            data.add(currentDateTime);
            add1UnitToCalendar(tempCalendar);
            for (Map<String, Object> objJob : objList) {
                if (currentDateTime.equals(objJob.get("time"))) {
                    data.add(objJob.get(key).toString());
                }
            }
            if (data.size() == 1) {
                data.add(DEFAULT_VALUE);
            }
            dataList.add(data);
        }
        jobMap.put("label", label);
        jobMap.put("data", dataList);
        return jobMap;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public String getEndTime() {
        return endTime.get();
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }

    public String[] getUsers() {
        return users.get();
    }

    public void setUsers(String users) {
        if (StringUtil.isNotEmpty(users)) {
            this.users.set(StringUtil.trimSplitByComma(users));
        }
    }

    public String[] getGroups() {
        return groups.get();
    }

    public void setGroups(String groups) {
        if (StringUtil.isNotEmpty(groups)) {
            this.groups.set(StringUtil.trimSplitByComma(groups));
        }
    }

    public String[] getProjects() {
        return projects.get();
    }

    public void setProjects(String projects) {
        if (StringUtil.isNotEmpty(projects)) {
            this.projects.set(StringUtil.trimSplitByComma(projects));
        }
    }

    public String[] getLicenses() {
        return licenses.get();
    }

    public void setLicenses(String licenses) {
        if (StringUtil.isNotEmpty(licenses)) {
            this.licenses.set(StringUtil.trimSplitByComma(licenses));
        }
    }

    public String[] getFeatures() {
        return features.get();
    }

    public void setFeatures(String features) {
        if (StringUtil.isNotEmpty(features)) {
            this.features.set(StringUtil.trimSplitByComma(features));
        }
    }

    public String[] getApplications() {
        return applications.get();
    }

    public void setApplications(String applications) {
        if (StringUtil.isNotEmpty(applications)) {
            this.applications.set(StringUtil.trimSplitByComma(applications));
        }
    }

    public void setTimeUnitEnum(String timeUnitEnum) {
        this.timeUnitEnum.set(TimeUnitEnum.valueOf(timeUnitEnum));
    }

    public void setReportFilterEnum(String reportFilterEnum) {
        this.reportFilterEnum.set(ReportFilterEnum.valueOf(reportFilterEnum));
    }

    public void setResourceEnum(String resourceEnum) {
        this.resourceEnum.set(ResourceEnum.valueOf(resourceEnum));
    }
    
    public void setShareStorageEnum(String shareStorageEnum) {
        this.shareStorageEnum.set(ShareStorageEnum.valueOf(shareStorageEnum));
    }
    
}
