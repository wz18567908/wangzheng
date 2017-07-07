package com.clustertech.cloud.gui.dao.report;

import java.text.ParseException;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.clustertech.cloud.gui.dao.BaseDao;
import com.clustertech.cloud.gui.domain.report.LicenseDateConfig;
import com.clustertech.cloud.gui.utils.CloudConstants.LicenseConfigDateEnum;
import com.clustertech.cloud.gui.utils.CloudConstants.OrderByEnum;

@Repository
@SuppressWarnings("unchecked")
public class LicenseDateConfigDao extends BaseDao<LicenseDateConfig> {

    public List<LicenseDateConfig> getWorkTimeData() {
        return getEntityList("dateType", "WorkTime", "startTime", OrderByEnum.ASC);
    }

    public List<LicenseDateConfig> getTimeDataByDateType(LicenseConfigDateEnum dateType) throws ParseException {
        return getEntityList("dateType", dateType.toString(), "startTime", OrderByEnum.ASC);
    }

    public List<String> getMinuteTimeLength(String startTime, String endTime) throws ParseException {
        String hql = "select round(timestampdiff(second," + "'" + startTime + "'," + "'" + endTime + "'"
                + ")/60) as timeLength";
        return getQuery(hql).list();
    }
}
