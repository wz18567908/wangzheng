#!/bin/bash

basePath=$(cd `dirname $0`;pwd)

function is_root() {
    if [ `id -u` -eq 0 ]; then
        return 0;
    fi
    return 1;
}

function is_empty_str() {
    if [ -z "$*" ]; then
        return 0
    fi
    return 1
}

function error() {
    echo "ERROR::[`date +'%Y-%m-%d %H:%M:%S'`] ($0) $*">>$basePath/logs/createjob.log
}

is_empty_str "$jobEnv_USERNAME"
if [ $? -eq 0 ]; then
    error "Submmiter is empty."
    exit 255
fi

is_empty_str "$jobEnv_JOBNAME"
if [ $? -eq 0 ]; then
    error "Job name is empty."
    exit 255
fi


is_empty_str "$jobEnv_COMMAND"
if [ $? -eq 0 ]; then
    error "Job command is empty."
    exit 255
fi

is_empty_str "$jobEnv_QUEUE"
if [ $? -gt 0 ]; then
    QUEUE="-q $jobEnv_QUEUE"
fi

COMMAND="bsub -J $jobEnv_JOBNAME $QUEUE $jobEnv_COMMAND "

COUT=`su $jobEnv_USERNAME -c "$COMMAND" 2>&1`
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    COUT=`echo $COUT | cut -d\< -f2 | cut -d\> -f1 `
fi
echo $COUT
exit $EXIT_CODE
