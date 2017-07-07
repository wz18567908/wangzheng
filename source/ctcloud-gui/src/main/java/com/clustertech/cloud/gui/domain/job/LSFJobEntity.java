package com.clustertech.cloud.gui.domain.job;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name="LSF_LIVE_JOB")
public class LSFJobEntity implements Serializable {
    private static final long serialVersionUID = 8936343890729821185L;

    @Id
    @Column(name="JOB_ID")
    public long jobId;

    @Id
    @Column(name="JOB_ARRAY_INDEX")
    public int arrayIndex;

    @Id
    @Column(name="CLUSTER_NAME")
    public String clusterName;

    @Column(name="EXIT_STATUS")
    public int exitStatus;

    @Column(name="NUM_PROCESSORS")
    public int numProcessors;

    @Column(name="USER_NAME")
    public String userName;

    @Column(name="PROJECT_NAME")
    public String projectName;

    @Column(name="JOB_STATUS")
    public String jobStatus;

    @Column(name="FROM_HOST")
    public String fromHost;

    @Column(name="COMMAND")
    public String command;

    @Column(name="WORKLOAD_TYPE")
    public String workLoadType;

    @Column(name="JOB_NAME")
    public String jobName;

    @Column(name="SUBMIT_TIME")
    public Date submitTime;

    @Column(name="START_TIME")
    public Date startTime;

    @Column(name="END_TIME")
    public Date endTime;

    @Column(name="QUEUE")
    public String queue;

    @Column(name="EXEC_HOSTSTR")
    public String execHosts;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public String getWorkLoadType() {
        return workLoadType;
    }

    public void setWorkLoadType(String workLoadType) {
        this.workLoadType = workLoadType;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getNumProcessors() {
        return numProcessors;
    }

    public void setNumProcessors(int numProcessors) {
        this.numProcessors = numProcessors;
    }

    public String getExecHosts() {
        return execHosts;
    }

    public void setExecHosts(String execHosts) {
        this.execHosts = execHosts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
