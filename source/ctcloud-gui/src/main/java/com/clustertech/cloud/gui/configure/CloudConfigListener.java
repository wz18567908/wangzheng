package com.clustertech.cloud.gui.configure;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CloudConfigListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String cloudTop = context.getInitParameter("cloudTopLocation");
        context.setAttribute("cloudConfigureInfo", CloudConfigureInfoFactory.getInstance(cloudTop));
    }
}
