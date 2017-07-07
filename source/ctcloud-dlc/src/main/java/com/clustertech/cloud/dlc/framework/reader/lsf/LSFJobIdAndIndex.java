package com.clustertech.cloud.dlc.framework.reader.lsf;

public class LSFJobIdAndIndex {
    private long jobId;
    private int jobIndex;

    public long getJobId() {
        return jobId;
    }
    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getJobIndex() {
        return jobIndex;
    }
    public void setJobIndex(int jobIndex) {
        this.jobIndex = jobIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            if (this.getClass() == obj.getClass()) {
                LSFJobIdAndIndex lsfJobIdAndIndex = (LSFJobIdAndIndex) obj;
                if (this.getJobId() == lsfJobIdAndIndex.getJobId()
                        && this.getJobIndex() == lsfJobIdAndIndex.getJobIndex()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}
