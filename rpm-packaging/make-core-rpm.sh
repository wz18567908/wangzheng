#!/bin/sh

function checkJava
{
    JAVA="${JAVA_HOME}/bin/java"
    if [ ! -f ${JAVA} ]; then
        echo "The evironement variable JAVA_HOME is not set correctlly."
        exit
    fi

    ${JAVA} -version 2>&1 | grep "java version" | grep -q  "1.8.0_112"
    if [ $? -ne 0 ]; then
        echo "Only JDK with version 1.8.0_112 is supported."
        exit 1
    fi

    ${JAVA} -version 2>&1 | grep -q "Java HotSpot(TM) 64-Bit Server VM"
    if [ $? -ne 0 ];then
        echo "Only JDK with version 64 bit is allowed."
        exit 1
    fi

    if [ -z ${JRE_LOCATION} ];then
        echo "The evironement variable JRE_LOCATION is not set correctlly."
        exit 1
    fi

    if [ ! -f "${JRE_LOCATION}/jre-8u112-linux-x64.tar.gz" ]; then
        echo "The jre-8u112-linux-x64.tar.gz can't be found in ${JRE_LOCATION}"
        exit 1
    fi
}
checkJava

if [ -z "${LSF_TOP}" ] || [ ! -f ${LSF_TOP}/conf/profile.lsf ]; then
    echo "Failed to find the environment variable LSF_TOP!"
    exit 1
fi

if [ -z ${TOMCAT_LOCATION} ];then
    echo "The evironement variable TOMCAT_LOCATION is not set correctlly."
    exit 1
fi

if [ ! -f "${TOMCAT_LOCATION}/apache-tomcat-8.5.6.tar.gz" ]; then
    echo "The apache-tomcat-8.5.6.tar.gz can't be found in ${TOMCAT_LOCATION}"
    exit 1
fi

set -x
APPEND=""
THESUBDIR=""
if [ "$1" = "src" ] ; then
    APPEND="-bs"
    THESUBDIR="SRPMS"
else
    APPEND="-bb"
    THESUBDIR="RPMS"
fi
SPECFILE=ctcloud-core.spec
NAME=`cat $SPECFILE | grep ^Name: | sed 's/Name://;s/\t//g;s/\s//g'`
VERSION=`cat $SPECFILE | grep ^Version: | sed 's/Version://;s/\t//g;s/\s//g'`
SOURCE=`cat $SPECFILE | grep ^Source: | sed 's/Source://;s/\t//g;s/\s//g'`
TARBALL=`echo $SOURCE | sed "s/%{name}/$NAME/;s/%{version}/$VERSION/"`
SRCDIR="$NAME-$VERSION"
RPMROOT=/tmp/rpm-ctcloud-core.$$

function cleanup ()
{
    find $RPMROOT/$THESUBDIR -type f -exec mv -f '{}' . \;
    rm -rf $RPMROOT
}
trap cleanup SIGHUP SIGINT SIGTERM EXIT
echo "%_topdir $RPMROOT" > ~/.rpmmacros
echo '%define debug_package %{nil}' >> ~/.rpmmacros

mkdir $RPMROOT
for subfolder in SOURCES BUILD RPMS SRPMS ROOT $SRCDIR
do
    mkdir "$RPMROOT/$subfolder"
done

exepath=$PWD
cd ..
rsync -av --progress . "$RPMROOT/$SRCDIR"  --exclude "rpm-packaging" --exclude ".git" --exclude "view"
cd $exepath

if [[ $SOURCE == *.tar.gz ]]; then
    tar -czf "$RPMROOT/SOURCES/$TARBALL" -C $RPMROOT "$SRCDIR"
elif [[ $SOURCE == *.tar.bz2$ ]]; then
    tar -cjf "$RPMROOT/SOURCES/$TARBALL" -C $RPMROOT "$SRCDIR"
elif [[ $SOURCE == *.tar ]]; then
    tar -cf "$RPMROOT/SOURCES/$TARBALL" -C $RPMROOT "$SRCDIR"
else
    echo "Source Tarball cannot be created"
    exit;
fi

# If rpm of your build machine has higher version then your target machine,
# for eg, your build machine is RHEL6 but the target machine is RHEL5,
# there may be dependency problem
#   Package chess-report-core needs rpmlib(FileDigests) <= 4.6.0-1
#   Package chess-report-core needs rpmlib(PayloadIsXz) <= 5.2-1
# The --define solve the dependency problem, see this link for reference
# http://madhuscribblings.wordpress.com/2013/03/04/rpm-error-failed-dependencies-rpmlibfiledigests-4-6-0-1-rpmlibpayloadisxz-5-2-1/
rpmbuild $APPEND $SPECFILE --buildroot $RPMROOT/ROOT --define "_binary_filedigest_algorithm  1"  --define "_binary_payload 1"
