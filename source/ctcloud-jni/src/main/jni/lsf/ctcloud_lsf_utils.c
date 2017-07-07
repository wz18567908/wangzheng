#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#include "ctcloud_lsf_utils.h"

#include <lsf/lsf.h>
#include <lsf/lsbatch.h>
void throwLSFBatchException(JNIEnv *env, const char *msg) {
    jclass cls = (*env)->FindClass(env, "com/clustertech/cloud/jni/lsf/LSFBatchException");
    if (cls == NULL) {
        (*env)->ExceptionDescribe(env);
        return;
    }

    const char *errMsg;
    if (msg != NULL) {
        errMsg = msg;
    } else {
        errMsg = lsb_sysmsg();
        if (errMsg == NULL) {
            errMsg = lsb_sysmsg();
            if (errMsg == NULL) {
                errMsg = "Unknown error: unable to get error message from LSBLIB";
            }
        }
    }
    (*env)->ThrowNew(env, cls, errMsg);
}

char *getUTFString(JNIEnv *env, jstring jstr) {
    if (jstr == NULL) {
        return NULL;
    }
    const char *str = (*env)->GetStringUTFChars(env, jstr, NULL);
    if (str == NULL) {
        return NULL;
    }
    char *copy = strdup(str);
    (*env)->ReleaseStringUTFChars(env, jstr, str);
    return copy;
}

void freeUTFString(char *str) {
    if (str) {
        free(str);
    }
}
