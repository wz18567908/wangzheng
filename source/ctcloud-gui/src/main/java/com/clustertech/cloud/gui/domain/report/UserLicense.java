package com.clustertech.cloud.gui.domain.report;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name="USER_LICENSE_USAGE")
public class UserLicense implements Serializable {
    private static final long serialVersionUID = 8936343890729821185L;

    @Id
    @Column(name = "USER_NAME")
    public String userName;

    @Id
    @Column(name = "GROUP_NAME")
    public String groupName;

    @Id
    @Column(name = "LICENSE_NAME")
    public String licenseName;

    @Id
    @Column(name = "FEATURE_NAME")
    public String featureName;

    @Id
    @Column(name = "START_TIME")
    public Date startTime;

    @Column(name = "END_TIME")
    public Date endTime;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
