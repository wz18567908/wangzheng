package com.clustertech.cloud.dlc.framework.reader.lsf;

public enum LSFJobEventsEnum {
    // lsb.events status
    JOB_NEW,           // no index position;
    JOB_START,         // index position:15;
    JOB_START_ACCEPT,  // index position:7;
    JOB_EXECUTE,       // index position:11;
    JOB_STATUS,        // index position:12/31;
}
