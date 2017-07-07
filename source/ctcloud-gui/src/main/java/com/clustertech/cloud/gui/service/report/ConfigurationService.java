package com.clustertech.cloud.gui.service.report;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clustertech.cloud.gui.dao.report.LicenseDateConfigDao;
import com.clustertech.cloud.gui.dao.report.ReportSettingDao;
import com.clustertech.cloud.gui.domain.report.LicenseDateConfig;
import com.clustertech.cloud.gui.domain.report.ReportSetting;
import com.clustertech.cloud.gui.exception.CTCloudException;

@Service
@Transactional
public class ConfigurationService {
    public static final Logger logger = Logger.getLogger(ConfigurationService.class);

    @Autowired
    private ReportSettingDao reportSettingDao;
    @Autowired
    private LicenseDateConfigDao licenseDateConfigDao;

    public List<ReportSetting> getReportSetting() {
        return reportSettingDao.getEntityList();
    }

    public void addLicenseDateConfig(LicenseDateConfig licenseDateConfig, Map<String, Object> primaryKeyValueMap)
            throws CTCloudException {
        licenseDateConfigDao.distinctSave(licenseDateConfig, primaryKeyValueMap);
    }

    public void deleteLicenseDateConfig(String id) {
        licenseDateConfigDao.delete("LicenseDateConfig", "id", id);
    }

    public List<LicenseDateConfig> getAllLicenseDateConfig() {
        return licenseDateConfigDao.getEntityList();
    }
}
