#ifndef _Included_utils
#define _Included_utils

#include <jni.h>
#include <stdlib.h>

/**
 * Throws LSFBatchException.
 * If 'msg' is supplied, it is used as error message, otherwise lsb_sysmsg() is used.
 */
void throwLSFBatchException(JNIEnv *env, const char *msg);

/**
 * Returns C string from Java String or NULL.
 * Memory allocated for C string should be then released with freeUTFString().
 */
char *getUTFString(JNIEnv *env, jstring jstr);
void freeUTFString(char *str);

#endif /* _Included_utils */
