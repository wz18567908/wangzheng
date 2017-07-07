package com.clustertech.cloud.gui.configure;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CloudConfigureInfo {
    private String cloudTop;

    public String getCloudTop() {
        return cloudTop;
    }
    public void setCloudTop(String cloudTop) {
        this.cloudTop = cloudTop;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
