package com.clustertech.cloud.gui.utils;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertyConfigurer extends PropertyPlaceholderConfigurer {

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties props) throws BeansException {
        DesEncrypter desEncrypter = DesEncrypter.getInstance();
        String username = props.getProperty(CloudConstants.JDBC_USERNAME);
        String password = props.getProperty(CloudConstants.JDBC_PASSWORD);
        props.setProperty(CloudConstants.JDBC_USERNAME, desEncrypter.decrypt(username));
        props.setProperty(CloudConstants.JDBC_PASSWORD, desEncrypter.decrypt(password));
        super.processProperties(beanFactoryToProcess, props);
    }
}
