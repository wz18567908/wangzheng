%define _prefix /usr/clustertech/ctcloud
%define _release 1ct

Name:           ctcloud-core
Version:        1.0
Release:        %{_release}%{dist}
Summary:        Clustertech cloud core
Prefix:         %{_prefix}

Group:          System Environment/Libraries
License:        Proprietary License. Copyright (C) 2017 Clustertech Ltd.
URL:            http://www.clustertech.com
Source:         %{name}-%{version}.tar

BuildRequires:  tar

Autoreq:        no

%description
core components of clustertech cloud module

%prep
%setup -q

%build
cd $RPM_BUILD_DIR/%{name}-%{version}/source
mvn package

# build and copy jni files
LSF_VERSION=`cat ${LSF_TOP}/conf/lsf.conf | grep -w "LSF_VERSION" | awk -F "=" '{print $2}'`
LSF_LIB_DIR="${LSF_TOP}/${LSF_VERSION}/linux2.6-glibc2.3-x86_64/lib"
LSF_INCLUDE_DIR="${LSF_TOP}/${LSF_VERSION}/include"
JNI_LIB64="ctcloud-distrib/target/ctcloud-distrib-1.0-bin/jni/lib64"
sed -i "s:__INCLUDE_DIR__:${LSF_INCLUDE_DIR}:g" ${JNI_LIB64}/Makefile
cp -rf ${JNI_LIB64}/* ${LSF_LIB_DIR}
cd ${LSF_LIB_DIR}
make
rm -rf com_clustertech_cloud_jni_lsf_LSFBatch.* ctcloud_lsf_utils.* Makefile
mv -f liblsb4j.so $RPM_BUILD_DIR/%{name}-%{version}/source/${JNI_LIB64}
cd -

%install
rm -rf $RPM_BUILD_ROOT

# copy dlc, dbconfig and jni files
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/dlc
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/dlc/logs

cd source/ctcloud-distrib/target/ctcloud-distrib-1.0-bin/dlc
cp -rf * $RPM_BUILD_ROOT/%{_prefix}/dlc
cp -rf ../dbconfig/bin/* $RPM_BUILD_ROOT/%{_prefix}/dlc/bin
cp -rf ../dbconfig/conf/* $RPM_BUILD_ROOT/%{_prefix}/dlc/conf
cp -rf ../dlc/lib/* $RPM_BUILD_ROOT/%{_prefix}/dlc/lib
cp -rf ../jni/lib64 $RPM_BUILD_ROOT/%{_prefix}
cd -

# copy database schema and scripts
cp -rf $RPM_BUILD_DIR/%{name}-%{version}/database $RPM_BUILD_ROOT/%{_prefix}

# copy and decompression jre
JRE_FILE_NAME="jre-8u112-linux-x64.tar.gz"
cp -rf ${JRE_LOCATION}/${JRE_FILE_NAME} $RPM_BUILD_ROOT/%{_prefix}
tar -xvf $RPM_BUILD_ROOT/%{_prefix}/${JRE_FILE_NAME} -C $RPM_BUILD_ROOT/%{_prefix}
mv -f $RPM_BUILD_ROOT/%{_prefix}/jre1.8.0_112 $RPM_BUILD_ROOT/%{_prefix}/jre
rm -rf $RPM_BUILD_ROOT/%{_prefix}/${JRE_FILE_NAME}

# copy and decompression tomcat
TOMCAT_FILE_NAME="apache-tomcat-8.5.6.tar.gz"
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/gui
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/gui/bin
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/gui/etc
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/gui/etc/logs
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/gui/conf
cp -rf ${TOMCAT_LOCATION}/${TOMCAT_FILE_NAME} $RPM_BUILD_ROOT/%{_prefix}/gui
cp -rf source/ctcloud-gui/src/main/bin/* $RPM_BUILD_ROOT/%{_prefix}/gui/bin
cp -rf source/ctcloud-gui/src/main/etc/* $RPM_BUILD_ROOT/%{_prefix}/gui/etc
cp -rf source/ctcloud-gui/src/main/conf/* $RPM_BUILD_ROOT/%{_prefix}/gui/conf
tar -xvf $RPM_BUILD_ROOT/%{_prefix}/gui/${TOMCAT_FILE_NAME} -C $RPM_BUILD_ROOT/%{_prefix}/gui
mv -f  $RPM_BUILD_ROOT/%{_prefix}/gui/apache-tomcat-8.5.6 $RPM_BUILD_ROOT/%{_prefix}/gui/tomcat
rm -rf $RPM_BUILD_ROOT/%{_prefix}/gui/${TOMCAT_FILE_NAME}

# copy gui to the tomcat
unzip source/ctcloud-gui/target/ctcloud.war -d $RPM_BUILD_ROOT/%{_prefix}/gui/tomcat/webapps/ctcloud

%pre
if [ -z "${LSF_TOP}" ] || [ ! -f ${LSF_TOP}/conf/lsf.conf ]; then
    echo "Failed to find the environment variable LSF_TOP!"
    exit 1
fi

%post
# update config files
sed -i "s:__CTDLC_TOP__:$RPM_INSTALL_PREFIX/dlc:g" $RPM_INSTALL_PREFIX/dlc/conf/cshrc.dlc
sed -i "s:__CTDLC_TOP__:$RPM_INSTALL_PREFIX/dlc:g" $RPM_INSTALL_PREFIX/dlc/conf/profile.dlc
sed -i "s:__CTDLC_TOP__:$RPM_INSTALL_PREFIX/dlc:g" $RPM_INSTALL_PREFIX/dlc/etc/init.d/ctdlcd
sed -i "s:__LSF_TOP__:$LSF_TOP:g" $RPM_INSTALL_PREFIX/dlc/conf/profile.dlc
sed -i "s:__LSF_TOP__:$LSF_TOP:g" $RPM_INSTALL_PREFIX/dlc/conf/cshrc.dlc
sed -i "s:__CTCLOUD_TOP__:$RPM_INSTALL_PREFIX:g" $RPM_INSTALL_PREFIX/gui/conf/ctcloud.xml
sed -i "s:__CTCLOUD_TOP__:$RPM_INSTALL_PREFIX:g" $RPM_INSTALL_PREFIX/gui/tomcat/webapps/ctcloud/WEB-INF/web.xml

# copy the ctdlc service
cp -rf $RPM_INSTALL_PREFIX/dlc/etc/init.d/ctdlcd /etc/init.d
rm -rf $RPM_INSTALL_PREFIX/dlc/etc/init.d

%preun
#ctdlc
PIDS=`ps -ef | grep -w 'com.clustertech.cloud.dlc.framework.DLController' | grep -v 'grep' | awk '{print $2}'`
if [ "$PIDS" != "" ]; then
    kill -9 ${PIDS}
fi
rm -rf /etc/init.d/ctdlcd
rm -rf $RPM_INSTALL_PREFIX/*
#ctcloud
PIDS=`ps -ef | grep -w "$RPM_INSTALL_PREFIX/gui/tomcat" | grep -v 'grep' | awk '{print $2}'`
if [ "$PIDS" != "" ]; then
    kill -9 ${PIDS}
fi

%files
%defattr(755, root, root, 644)
%{_prefix}/dlc/bin/
%{_prefix}/dlc/etc/
%{_prefix}/dlc/conf/
%{_prefix}/dlc/lib/
%{_prefix}/dlc/logs/
%{_prefix}/lib64/
%{_prefix}/database/
%{_prefix}/jre/
%{_prefix}/gui/tomcat/
%{_prefix}/gui/bin/
%{_prefix}/gui/etc/
%{_prefix}/gui/conf/
