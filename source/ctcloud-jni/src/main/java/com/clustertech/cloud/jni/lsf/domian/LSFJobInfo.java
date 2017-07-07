package com.clustertech.cloud.jni.lsf.domian;

public class LSFJobInfo {
    private long jobId;
    private int arrayIndexId;
    private String userName;
    private String projectName;
    private int jobStatus;
    private String fromHost;
    private int numExHosts;
    private int exitStatus;
    private String parentGroup;
    private String jobName;
    private long submitTime;
    private long startTime;
    private long endTime;
    private String queue;
    private int numProcessors;
    private int maxNumProcessors;
    private String command;
    private Object[] execHosts;
    private String clusterName;

    public LSFJobInfo() {
    }

    public LSFJobInfo(long jobId, int arrayIndexId, String jobName, String userName,
            String projectName, String queue, String clusterName, int numExHosts,
            Object[] execHosts, String fromHost, int numProcessors, int maxNumProcessors,
            String command, long submitTime, long startTime, long endTime, int jobStatus,
            int exitStatus, String parentGroup) {
        this.jobId = jobId;
        this.arrayIndexId = arrayIndexId;
        this.userName = userName;
        this.jobStatus = jobStatus;
        this.exitStatus = exitStatus;
        this.jobName = jobName;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.projectName = projectName;
        this.queue = queue;
        this.numProcessors = numProcessors;
        this.maxNumProcessors = maxNumProcessors;
        this.command = command;
        this.execHosts = execHosts;
        this.clusterName = clusterName;
        this.fromHost = fromHost;
        this.numExHosts = numExHosts;
        this.parentGroup = parentGroup;
    }

    public long getJobId() {
        return jobId;
    }

    public int getArrayIndexId() {
        return arrayIndexId;
    }

    public String getUserName() {
        return userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public String getFromHost() {
        return fromHost;
    }

    public int getNumExHosts() {
        return numExHosts;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getParentGroup() {
        return parentGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getQueue() {
        return queue;
    }

    public int getNumProcessors() {
        return numProcessors;
    }

    public int getMaxNumProcessors() {
        return maxNumProcessors;
    }

    public String getCommand() {
        return command;
    }

    public Object[] getExecHosts() {
        return execHosts;
    }

    public String getClusterName() {
        return clusterName;
    }
}
