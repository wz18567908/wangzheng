package com.clustertech.cloud.gui.configure;

import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.utils.CloudConstants;
import com.clustertech.cloud.gui.utils.StringUtil;

public class CloudConfigureInfoFactory {
    private static CloudConfigureInfo cloudConfigureInfo = null;

    public synchronized static CloudConfigureInfo getInstance(String cloudTop) {
        if (cloudConfigureInfo == null) {
            String xmlMappingFile = cloudTop + CloudConstants.CLOUD_CONF_MAPPING;
            String xmlFile = cloudTop + CloudConstants.CLOUD_CONF;
            try {
                XMLReader xmlReader = new XMLReader(xmlMappingFile, xmlFile);
                cloudConfigureInfo = (CloudConfigureInfo) xmlReader.getConfigData();
            } catch (CTCloudException cte) {
                Logger logger = Logger.getLogger(CloudConfigureInfoFactory.class);
                logger.error(StringUtil.formatErrorLogger(cte, "getInstance", cloudTop));
            }
        }
        return cloudConfigureInfo;
    }
}
