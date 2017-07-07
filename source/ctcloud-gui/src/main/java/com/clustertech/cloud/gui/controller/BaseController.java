package com.clustertech.cloud.gui.controller;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.configure.CloudConfigureInfo;

public class BaseController<T extends Serializable> {
    protected Logger logger;
    protected SimpleDateFormat dateFormat;
    protected SimpleDateFormat dateFormat1;

    @SuppressWarnings({ "rawtypes" })
    public BaseController() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        logger = Logger.getLogger((Class) params[0]);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat1 = new SimpleDateFormat("HH:mm:ss");
    }

    protected CloudConfigureInfo getCloudConfigureInfo(HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        return (CloudConfigureInfo)context.getAttribute("cloudConfigureInfo");
    }
}
