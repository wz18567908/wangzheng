package com.clustertech.cloud.gui.domain.report;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "REPORT_SETTING")
public class ReportSetting implements Serializable {
    private static final long serialVersionUID = -8001896935196819758L;

    @Id
    @Column(name = "REPORT_NAME", nullable = false)
    private String reportName;

    @Column(name = "STATE", nullable = false)
    private short state;    //0:false; 1:ture;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
