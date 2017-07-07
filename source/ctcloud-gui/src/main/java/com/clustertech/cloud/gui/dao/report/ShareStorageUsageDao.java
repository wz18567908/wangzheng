package com.clustertech.cloud.gui.dao.report;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.clustertech.cloud.gui.dao.BaseDao;
import com.clustertech.cloud.gui.domain.report.ShareStorage;

@Repository
@SuppressWarnings("unchecked")
public class ShareStorageUsageDao extends BaseDao<ShareStorage> {

    public List<Map<String, Object>> getShareStorageUsageList(String timeFormat, String startTime, String endTime) {
        String hql = " select new map (date_format(timeStamp, '%1$s') as time, resourceName as storage, "
                + "usedGB+freeGB as total, avg(usedGB) as used, avg(freeGB) as free, avg(usage) as util)"
                   + " from ShareStorage where timeStamp between '%2$s' and '%3$s'";

        String hqlFormat = String.format(hql, timeFormat, startTime, endTime);

        return getQuery(hqlFormat).list();
    }
    
    public List<String> getShareStorageInfoList() {
        String hql = " select resourceName from ShareStorage group by resourceName";

        return getQuery(hql).list();
    }
}