#include <stdio.h>
#include <string.h>

#include "ctcloud_lsf_utils.h"
#include "com_clustertech_cloud_jni_lsf_LSFBatch.h"

#include <lsf/lsf.h>
#include <lsf/lsbatch.h>

/*
 * Class:     com_clustertech_cloud_jni_lsf_LSFBatch
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_clustertech_cloud_jni_lsf_LSFBatch_init(JNIEnv *env, jobject thiz, jstring appName) {
    char *utfName = getUTFString(env, appName);
    int rc = lsb_init(utfName);
    freeUTFString(utfName);
    if (rc < 0) {
        throwLSFBatchException(env, NULL);
        return;
    }
}

/*
 * Class:     com_clustertech_cloud_jni_lsf_LSFBatch
 * Method:    readJobInfo
 * Signature: (JILjava/lang/String;Ljava/lang/String;Ljava/lang/String;
 *             Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/Object;
 *             Ljava/lang/String;IIILjava/lang/String;JJJIIIIILjava/lang/String;
 *             IJJJJILjava/lang/String;FII)V
 */
JNIEXPORT jobjectArray JNICALL Java_com_clustertech_cloud_jni_lsf_LSFBatch_readJobInfo(
        JNIEnv *env, jobject thiz, jobject jobInfo, jlong jobId,
        jstring jobName, jstring userName, jstring queueName, jstring hostName,
        jint options) {

    char *jobNameStr = getUTFString(env, jobName);
    char *userNameStr = getUTFString(env, userName);
    char *queueNameStr = getUTFString(env, queueName);
    char *hostNameStr = getUTFString(env, hostName);

    int totalJobsNumber = lsb_openjobinfo(jobId, jobNameStr, userNameStr,
            queueNameStr, hostNameStr, options);
    if (totalJobsNumber < 0) {
            if (lsberrno != LSBE_NO_JOB) {
                throwLSFBatchException(env, NULL);
            }
            return;
        } else if (totalJobsNumber == 0) {
            return;
        }

    char* clusterName = ls_getclustername();

    jclass cls = (*env)->FindClass(env,
            "com/clustertech/cloud/jni/lsf/domian/LSFJobInfo");
    jmethodID constructor =
            (*env)->GetMethodID(env, cls, "<init>",
                    "(JILjava/lang/String;Ljava/lang/String;Ljava/lang/String;"
                    "Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/Object;"
                    "Ljava/lang/String;IILjava/lang/String;JJJIILjava/lang/String;)V");
    int more = totalJobsNumber;
    struct jobInfoEnt *job;
    jobjectArray array = (*env)->NewObjectArray(env, more, cls, NULL);
    int i;
    for (i = 0; i < more; ++i, ++job) {
        int address = i + 1;
        job = lsb_readjobinfo(&address);
        if (job == NULL) {
            if (lsberrno == LSBE_EOF) {
                lsb_perror(NULL);
            } else {
                throwLSFBatchException(env, NULL);
                // Exit with exception
                // Break instead of return to call lsb_closejobinfo() before exit
            }
        }
        jobjectArray exhosts;
        if (job->numExHosts != 0) {
                exhosts = (*env)->NewObjectArray(env, job->numExHosts,
                        (*env)->FindClass(env, "java/lang/String"), NULL);
                int j;
                for (j = 0; j < job->numExHosts; j++) {
                    (*env)->SetObjectArrayElement(env, exhosts, j,
                            (*env)->NewStringUTF(env, job->exHosts[j]));
                }
            } else {
                exhosts = NULL;
        }
        char *jobname, *pos;

        jobname = job->submit.jobName;
        if (LSB_ARRAY_IDX(job->jobId) > 0) {
            if (pos = strchr(jobname, '[')) {
                *pos = '\0';
                sprintf(jobname, "%s[%d]", jobname,
                        LSB_ARRAY_IDX(job->jobId));
            }
        }
        jobInfo = (*env)->NewObject(env, cls, constructor, (jlong) LSB_ARRAY_JOBID(job->jobId),
                (jint) LSB_ARRAY_IDX(job->jobId),
                (*env)->NewStringUTF(env, jobname),
                (*env)->NewStringUTF(env, job->user),
                (*env)->NewStringUTF(env, job->submit.projectName),
                (*env)->NewStringUTF(env, job->submit.queue),
                (*env)->NewStringUTF(env, clusterName),(jint) job->numExHosts,
                (jobjectArray) exhosts, (*env)->NewStringUTF(env, job->fromHost),
                (jint) job->submit.numProcessors,
                (jint) job->submit.maxNumProcessors,
                (*env)->NewStringUTF(env, job->submit.command),
                (jlong) job->submitTime, (jlong) job->startTime,
                (jlong) job->endTime, (jint) job->status,
                (jint) job->exitStatus, (*env)->NewStringUTF(env, job->parentGroup));
        if (jobInfo == NULL) {
            break;
        }
        (*env)->SetObjectArrayElement(env, array, i, jobInfo);
        if ((*env)->ExceptionCheck(env) == JNI_TRUE) {
            return NULL;
        }
    }

    // Do not call JNI methods after while (true) {...} loop: here can be pending exceptions.
    lsb_closejobinfo();

    freeUTFString(jobNameStr);
    freeUTFString(userNameStr);
    freeUTFString(queueNameStr);
    freeUTFString(hostNameStr);

    return array;
}
