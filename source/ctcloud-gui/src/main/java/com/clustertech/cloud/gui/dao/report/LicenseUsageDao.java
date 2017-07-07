package com.clustertech.cloud.gui.dao.report;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.clustertech.cloud.gui.dao.BaseDao;
import com.clustertech.cloud.gui.domain.report.License;

@Repository
@SuppressWarnings("unchecked")
public class LicenseUsageDao extends BaseDao<License> {

    public List<Map<String, Object>> getLicenseUsageList(String timeFormat, String startTime, String endTime,
            String hqlWhereClause) {
        String hql = " select new map (date_format(timeStamp, '%1$s') as time, licenseName as licenseName, "
                + "featureName as featureName, total as total, inUse as inUse)"
                   + " from License where timeStamp between '%2$s' and '%3$s'";
        hql += hqlWhereClause;

        String hqlFormat = String.format(hql, timeFormat, startTime, endTime);

        return getQuery(hqlFormat).list();
    }
}
