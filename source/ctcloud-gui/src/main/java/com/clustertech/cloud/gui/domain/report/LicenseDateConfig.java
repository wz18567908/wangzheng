package com.clustertech.cloud.gui.domain.report;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "LICENSE_DATE_CONFIG")
public class LicenseDateConfig implements Serializable {
    private static final long serialVersionUID = 6547043918595598026L;

    @Id
    @GenericGenerator(name="uuidGen", strategy="uuid")  
    @GeneratedValue(generator="uuidGen") 
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "DATE_TYPE", nullable = false)
    private String dateType;

    @Column(name = "START_TIME", nullable = false)
    public String startTime;

    @Column(name = "END_TIME", nullable = false)
    public String endTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
