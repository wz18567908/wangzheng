package com.clustertech.cloud.gui.dao.report;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.clustertech.cloud.gui.dao.BaseDao;
import com.clustertech.cloud.gui.domain.report.UserLicense;
import com.clustertech.cloud.gui.utils.CloudConstants.LicenseTimeEnum;

@Repository
@SuppressWarnings("unchecked")
public class UserLicenseUsageDao extends BaseDao<UserLicense> {

    public List<Map<String, Object>> getDateRange() {
        String hql = " select new map(min(date_format(startTime, '%Y-%m-%d')) as minDate,"
                   + " max(date_format(endTime, '%Y-%m-%d')) as maxDate) from UserLicense";
        return getQuery(hql).list();
    }
    
    public List<Map<String, Object>> test(String hqlTimeFormat, String startTime, String endTime,
            String whereClauseHql) {
//        String test = "select new map(userName as userName, licenseName as licenseName, featureName as featureName, "
//                + "date_format( '" + startTime +"', '%Y-%m-%d %H:%i:%S') as startTime, "
//                + "date_format('" + endTime + "', '%Y-%m-%d %H:%i:%S') as endTime) from UserLicense "
//                + "where startTime>='2017-06-01 00:00:00' and endTime<='2017-07-31 23:59:59'  and licenseName in ('snpslmd') "
//                + "and featureName in ('Laker_FPD') and userName in ('buhh') and groupName in ('unLinuxUser')";
//        String test = "select new map(userName as userName, licenseName as licenseName, featureName as featureName, "
//                + "date_format( case when startTime<'" + startTime +"' then '" + startTime +"' else startTime end, '%Y-%m-%d %H:%i:%S') as startTime, "
//                + "date_format(case when endTime>'" + endTime + "' then '" + endTime + "' else endTime end, '%Y-%m-%d %H:%i:%S') as endTime) from UserLicense "
//                + "where startTime>='2017-06-01 00:00:00' and endTime<='2017-07-31 23:59:59'  and licenseName in ('snpslmd') "
//                + "and featureName in ('Laker_FPD') and userName in ('buhh') and groupName in ('unLinuxUser')";
//        String test1 = "select new map(userName as userName, licenseName as licenseName, featureName as featureName) from UserLicense where startTime>='2017-06-01 00:00:00' and endTime<='2017-07-31 23:59:59'  and licenseName in ('snpslmd') and featureName in ('Laker_FPD') and userName in ('buhh') and groupName in ('unLinuxUser')";
        String test2 = " select new map (userName as userName, licenseName as licenseName, featureName as featureName, date_format( case when startTime<'2017-06-01 00:00:00' then '2017-06-01 00:00:00' else startTime end, '%Y-%m-%d %H:%i:%S') as startTime, date_format(case when endTime>'2017-07-31 23:59:59' then '2017-07-31 23:59:59' else endTime end, '%Y-%m-%d %H:%i:%S') as endTime) from UserLicense where startTime>='2017-06-01 00:00:00' and endTime<='2017-07-31 23:59:59'  and licenseName in ('snpslmd') and featureName in ('Laker_FPD') and userName in ('buhh') and groupName in ('unLinuxUser')";
        return getQuery(test2).list();
    }

    public List<Map<String, Object>> getUserLicenseUsageList(String hqlTimeFormat, String startTime, String endTime,
            String whereClauseHql) {
        String hql = " select new map (userName as userName, licenseName as licenseName, featureName as featureName,"
                + " date_format( case when startTime<'%2$s' then '%2$s' else startTime end, '%1$s') as startTime,"
               + " date_format(case when endTime>'%3$s' then '%3$s' else endTime end, '%1$s') as endTime)"
               + " from UserLicense where startTime<='%3$s' and endTime>='%2$s' %4$s";

        String hqlFormat = String.format(hql, hqlTimeFormat, startTime, endTime, whereClauseHql);

        return getQuery(hqlFormat).list();
    }

    public List<Map<String, Object>> getUserLicenseUsageDetail(LicenseTimeEnum licenseTimeEnum, String hqlTimeFormat,
            String startTime, String endTime, String hqlWhereClause) {
        String hql = "select userName, groupName, licenseName, featureName, date_format( case 1 when startTime<'%2$s'"
                + " then '%2$s' else startTime end, '%1$s') as startTime, (date_format(case 1 when endTime>'%3$s'"
                + " then '%3$s' else endTime end, '%1$s')) as endTime from UserLicense"
                + " where startTime<='%3$s' and endTime>='%2$s' %4$s";
        hql += " and timediff(endTime, startTime)>'00:00:00' order by featureName";

        if (LicenseTimeEnum.TOTAL_TIME == licenseTimeEnum) {
            hql = "select *, round(timestampdiff(second, lic.startTime, lic.endTime)/60, 2) as totalTimeLength"
                    + " from (" + hql + ") as lic";
        }

        String hqlFormat = String.format(hql, hqlTimeFormat, startTime, endTime, hqlWhereClause);

        return getQuery(hqlFormat).list();
    }
}
