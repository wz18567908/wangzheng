package com.clustertech.cloud.gui.domain.report;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name="LICENSE_USAGE")
public class License implements Serializable {
    private static final long serialVersionUID = 8936343890729821185L;

    @Id
    @Column(name = "TIME_STAMP")
    public Date timeStamp;

    @Id
    @Column(name = "LICENSE_NAME")
    public String licenseName;

    @Id
    @Column(name = "FEATURE_NAME")
    public String featureName;

    @Column(name = "TOTAL")
    public int total;

    @Column(name = "IN_USE")
    public int inUse;

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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getInUse() {
        return inUse;
    }

    public void setInUse(int inUse) {
        this.inUse = inUse;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
