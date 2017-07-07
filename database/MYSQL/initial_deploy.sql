/*==============================================================*/
/* drop and create database and set character                   */
/*==============================================================*/
DROP DATABASE IF EXISTS ctcloud;

CREATE DATABASE ctcloud DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

/*==============================================================*/
/* grant privileges and create user by default                  */
/*==============================================================*/
GRANT CREATE, SELECT, INSERT, UPDATE, DELETE ON ctcloud.* TO 'ctcloud'@'localhost' IDENTIFIED BY 'ctcloud';
GRANT CREATE, SELECT, INSERT, UPDATE, DELETE ON ctcloud.* TO 'ctcloud'@'%' IDENTIFIED BY 'ctcloud';

/*==============================================================*/
/* switch to database "ctcloud"                                */
/*==============================================================*/
use ctcloud;

/*==============================================================*/
/* TABLE: LSF_LIVE_JOB                                            */
/*==============================================================*/

DROP TABLE IF EXISTS LSF_LIVE_JOB;
CREATE TABLE LSF_LIVE_JOB (
    JOB_ID bigint(20) unsigned NOT NULL,
    JOB_ARRAY_INDEX int(10) unsigned NOT NULL,
    NUM_PROCESSORS int(10) unsigned DEFAULT 0,
    SUBMIT_TIME timestamp NULL DEFAULT NULL,
    START_TIME timestamp NULL DEFAULT NULL,
    END_TIME timestamp NULL DEFAULT NULL,
    JOB_NAME varchar(128) COLLATE utf8_bin DEFAULT NULL,
    USER_NAME varchar(128) COLLATE utf8_bin DEFAULT NULL,
    QUEUE varchar(128) COLLATE utf8_bin DEFAULT NULL,
    EXEC_HOSTSTR varchar(1024) COLLATE utf8_bin DEFAULT NULL,
    FROM_HOST varchar(512) COLLATE utf8_bin DEFAULT NULL,
    COMMAND varchar(512) COLLATE utf8_bin DEFAULT NULL,
    PROJECT_NAME varchar(1024) COLLATE utf8_bin DEFAULT NULL,
    EXIT_STATUS varchar(30) COLLATE utf8_bin DEFAULT NULL,
    JOB_STATUS varchar(30) COLLATE utf8_bin DEFAULT NULL,
    WORKLOAD_TYPE varchar(10) COLLATE utf8_bin NOT NULL,
    CLUSTER_NAME varchar(128) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (JOB_ID, JOB_ARRAY_INDEX, CLUSTER_NAME)
) ENGINE=InnoDB;

/*==============================================================*/
/* TABLE: LSF_HISTORY_JOB                                            */
/*==============================================================*/

DROP TABLE IF EXISTS LSF_HISTORY_JOB;
CREATE TABLE LSF_HISTORY_JOB (
    INSERT_SEQ bigint(20) NOT NULL AUTO_INCREMENT,
    JOB_ID bigint(20) unsigned NOT NULL,
    JOB_ARRAY_INDEX int(10) unsigned NOT NULL,
    SUBMIT_TIME timestamp NULL DEFAULT NULL,
    START_TIME timestamp NULL DEFAULT NULL,
    END_TIME timestamp NULL DEFAULT NULL,
    JOB_NAME varchar(128) COLLATE utf8_bin DEFAULT NULL,
    USER_NAME varchar(128) COLLATE utf8_bin DEFAULT NULL,
    QUEUE varchar(128) COLLATE utf8_bin DEFAULT NULL,
    EXEC_HOSTSTR varchar(1024) COLLATE utf8_bin DEFAULT NULL,
    FROM_HOST varchar(512) COLLATE utf8_bin DEFAULT NULL,
    COMMAND varchar(512) COLLATE utf8_bin DEFAULT NULL,
    PROJECT_NAME varchar(1024) COLLATE utf8_bin DEFAULT NULL,
    EXIT_STATUS varchar(30) COLLATE utf8_bin DEFAULT NULL,
    JOB_STATUS varchar(30) COLLATE utf8_bin DEFAULT NULL,
    WORKLOAD_TYPE varchar(10) COLLATE utf8_bin NOT NULL,
    CLUSTER_NAME varchar(128) COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (INSERT_SEQ)
) ENGINE=InnoDB;

/*==============================================================*/
/* TRIGGER: lsf_job_synchronization                             */
/*==============================================================*/
DROP TRIGGER IF EXISTS lsf_job_synchronization;
DELIMITER ;;
CREATE TRIGGER lsf_job_synchronization AFTER INSERT ON LSF_LIVE_JOB FOR EACH ROW
BEGIN
    INSERT INTO LSF_HISTORY_JOB
        (JOB_ID, JOB_ARRAY_INDEX, JOB_NAME, USER_NAME, QUEUE, SUBMIT_TIME, START_TIME, END_TIME,
         EXEC_HOSTSTR, FROM_HOST, COMMAND, PROJECT_NAME, EXIT_STATUS, JOB_STATUS, WORKLOAD_TYPE, CLUSTER_NAME)
    VALUES
        (new.JOB_ID, new.JOB_ARRAY_INDEX, new.JOB_NAME, new.USER_NAME, new.QUEUE, new.SUBMIT_TIME,
         new.START_TIME, new.END_TIME, new.EXEC_HOSTSTR, new.FROM_HOST, new.COMMAND, new.PROJECT_NAME,
         new.EXIT_STATUS, new.JOB_STATUS, new.WORKLOAD_TYPE, new.CLUSTER_NAME);
END
;;
DELIMITER ;
