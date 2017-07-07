package com.clustertech.cloud.gui.utils;

public class LSFConstants {
    public static final int INET6_ADDRSTRLEN = 46;
    public static final int MAXLSFNAMELEN = 40;
    public static final int MAXHOSTNAMELEN = 64;
    public static final int MAXLINELEN = 512;
    public static final int MAXRESDESLEN = 256;
    public static final int MAXTYPES = 128;
    public static final int MAXMODELS = 1024 + 2;
    public static final int MAXLSFNAMELEN_70_EP1 = 128;
    public static final int NUM_JGRP_COUNTERS = 14;
    public static final int LSF_RLIM_NLIMITS = 12;

    /**
     * LSF Job Status
     */
    public static final int JOB_STAT_NULL = 0x00;
    public static final int JOB_STAT_PEND = 0x01;
    public static final int JOB_STAT_PSUSP = 0x02;
    public static final int JOB_STAT_RUN = 0x04;
    public static final int JOB_STAT_SSUSP = 0x08;
    public static final int JOB_STAT_USUSP = 0x10;
    public static final int JOB_STAT_EXIT = 0x20;
    public static final int JOB_STAT_DONE = 0x40;
    public static final int JOB_STAT_PDONE = 0x80;
    public static final int JOB_STAT_PERR = 0x100;
    public static final int JOB_STAT_WAIT = 0x200;
    public static final int JOB_STAT_RUNKWN = 0x8000;
    public static final int JOB_STAT_UNKWN = 0x10000;
    public static final int EXIT_ZOMBIE = 0x00000002; /*
                                                       * Job killed while host
                                                       * unavailable
                                                       */
    public static final int JOB_STAT_SUSP = 0x1A; /*
                                                   * job is suspended, it is add
                                                   * for job notification PSUSP
                                                   * + SSUSP + USUSP
                                                   */
    public static final int JGRP_NODE_JOB = 1; // this structure stores a normal
                                               // batch job
    public static final int JGRP_NODE_GROUP = 2; // this structure stores a job
                                                 // group
    public static final int JGRP_NODE_ARRAY = 3; // this structure stores a job
                                                 // array
    public static final int JGRP_NODE_FLOW = 4; // this structure stores a flow

    /**
     * options for lsb_openjobinfo
     */
    public static final int JOB_OPTIONS_ALL_JOB = 0x0001;
    public static final int JOB_OPTION_DONE_JOB = 0x0002;
    public static final int JOB_OPTION_PEND_JOB = 0x0004;
    public static final int JOB_OPTION_SUSP_JOB = 0x0008;
    public static final int JOB_OPTION_CUR_JOB = 0x0010;
    public static final int JOB_OPTION_LAST_JOB = 0x0020;
    public static final int JOB_OPTION_RUN_JOB = 0x0040;
    public static final int JOB_OPTION_JOBID_ONLY = 0x0080;
    public static final int JOB_OPTION_HOST_NAME = 0x0100;
    public static final int JOB_OPTION_NO_PEND_REASONS = 0x0200;
    public static final int JOB_OPTION_JGRP_INFO = 0x0400;
    public static final int JOB_OPTION_JGRP_RECURSIVE = 0x0800;
    public static final int JOB_OPTION_JGRP_ARRAY_INFO = 0x1000;
    public static final int JOB_OPTION_JOBID_ONLY_ALL = 0x2000;
    public static final int JOB_OPTION_ZOMBIE_JOB = 0x4000;
    public static final int JOB_OPTION_TRANSPARENT_MC = 0x8000;
    public static final int JOB_OPTION_EXCEPT_JOB = 0x10000;
    public static final int JOB_OPTION_MUREX_JOB = 0x20000;

    public static final String JOB_COMMAND_BRESUME_JOB = "resume";
    public static final String JOB_COMMAND_BKILL_JOB = "kill";
    public static final String JOB_COMMAND_BSTOP_JOB = "suspend";
    public static final String JOB_COMMAND_BREQUEUE_JOB = "requeue";
    public static final String JOB_COMMAND_BPEEK_JOB = "bpeek";

}
