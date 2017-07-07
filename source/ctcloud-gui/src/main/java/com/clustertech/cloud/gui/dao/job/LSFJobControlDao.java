package com.clustertech.cloud.gui.dao.job;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.clustertech.cloud.gui.dao.BaseDao;
import com.clustertech.cloud.gui.domain.job.LSFJobEntity;
import com.clustertech.cloud.gui.utils.Page;

@Repository
public class LSFJobControlDao extends BaseDao<LSFJobEntity> {
    public Page<LSFJobEntity> getLSFJobInfoPage(int pageNo, int pageSize, String currentUserName, String orderBy,
            String searchValue, String filter, Map<String, Object> filterData) {
        return getPage(pageNo, pageSize, currentUserName, orderBy, searchValue, filter, filterData);
    }
}
