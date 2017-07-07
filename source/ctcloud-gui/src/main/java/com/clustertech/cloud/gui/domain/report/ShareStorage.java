package com.clustertech.cloud.gui.domain.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name="SHARE_STORAGE_USAGE")
public class ShareStorage implements Serializable {
    private static final long serialVersionUID = -8564095205997094947L;

    @Id
    @Column(name = "TIME_STAMP", columnDefinition="TIMESTAMP")
    public Date timeStamp;

    @Id
    @Column(name = "RESOURCE_NAME")
    public String resourceName;
    
    @Column(name = "USED_GB", nullable = false, precision = 9, scale = 3)
    public BigDecimal usedGB;
    
    @Column(name = "FREE_GB", nullable = false, precision = 9, scale = 3)
    public BigDecimal freeGB;
    
    @Column(name = "USED_UTIL", nullable = false, precision = 5, scale = 2)
    public BigDecimal usage;
    
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public BigDecimal getUsedGB() {
        return usedGB;
    }

    public void setResourceName(BigDecimal usedGB) {
        this.usedGB = usedGB;
    }
    
    public BigDecimal getFreeGB() {
        return freeGB;
    }

    public void setFreeGB(BigDecimal freeGB) {
        this.freeGB = freeGB;
    }
    
    public BigDecimal getUsage() {
        return usage;
    }

    public void setUsage(BigDecimal usage) {
        this.usage = usage;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
