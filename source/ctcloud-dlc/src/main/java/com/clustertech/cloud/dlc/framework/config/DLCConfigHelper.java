/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.AppConfig;

/**
 * Provide utilities to parse and retrieve DLControlerConfig
 */
public class DLCConfigHelper {
    private final static Logger logger = LoggerHelper.getInstance().getLogger(
            LoggerType.DLC);
    private static final String dlcConfigDir = AppConfig.getInstance()
            .getProperty(AppConfig.ConfigKey.CONF_DIR) + File.separator + "dlc";
    private static DLCConfigHelper helper = null;
    private final Map<String, DLCConfig> dlcConfigMap = new HashMap<String, DLCConfig>();

    /**
     * Create an empty instance
     */
    private DLCConfigHelper() {
    }

    /**
     * Get the instance of the ServiceConfigHelper
     * @return
     */
    public synchronized final static DLCConfigHelper getInstance() {
        if (helper == null) {
            helper = new DLCConfigHelper();
        }
        return helper;
    }

    /**
     * Load all configuration files
     * @return
     * @throws ConfigParsingException
     */
    public synchronized final void load() throws ConfigParsingException {
        // List XML files under the directory
        File confDir = new File(dlcConfigDir);
        File[] confFiles = confDir.listFiles(new XMLListingFilter());

        if (confFiles == null || confFiles.length == 0) {
            throw new ConfigParsingException(
                    "No service configuration files found.");
        }

        StringBuffer strBuffer = new StringBuffer();
        // Loop on all files under the services directory
        for (File file : confFiles) {
            try {
                Map<String, DLCConfig> map = loadConfigFromXML(file);
                this.dlcConfigMap.putAll(map);
            }
            catch (Throwable th) {
                logger.error("Failed to parse the data loader controller file {}.", file.getAbsolutePath(), th);
                strBuffer.append(
                        String.format("Failed to parse the data loader controller file %s: %s.", file.getName(),
                                th.getMessage())).append("\n");
            }
        }

        if (strBuffer.length() > 0) {
            throw new ConfigParsingException(strBuffer.toString());
        }
    }

    /**
     * Load data loader controller files from XML
     * @param xmlFile
     * @return
     * @throws ConfigParsingException
     */
    public final static Map<String, DLCConfig> loadConfigFromXML(File xmlFile)
            throws ConfigParsingException {
        // Parase the given XML file to DOM
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = null;
        Document xmlDoc = null;

        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance("http://www.w3.org/2001/XMLSchema");
            File schemaFile = new File(xmlFile.getParent() + File.separator
                    + "dlc.xsd");
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            Source source = new StreamSource(xmlFile);
            validator.validate(source);
            dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(xmlFile);
            // xmlDoc.getDocumentElement().normalize();
        }
        catch (Throwable ex) {
            throw new ConfigParsingException(xmlFile.getAbsolutePath(), ex);
        }

        // Get the root element
        Element rootElement = xmlDoc.getDocumentElement();

        // Parse Dataloaders
        Element elemDataLoaders = ConfigParsingUtil.getElementByTagName(
                rootElement, "DataLoaders");
        if (elemDataLoaders == null) {
            throw new ConfigParsingException("No DataLoaders element found");
        }

        NodeList list = elemDataLoaders.getChildNodes();

        // Create a map
        Map<String, DLCConfig> dlcConfigMap = new HashMap<String, DLCConfig>();

        // Loop all DataLoader elements
        int len = list.getLength();
        for (int index = 0; index < len; index++) {
            Node node = list.item(index);
            if (node instanceof Element == false) {
                continue;
            }
            DLCConfig dlcConfig = parseDataLoader((Element) node);
            if (logger.isDebugEnabled()) {
                logger.debug(dlcConfig.toString());
            }
            dlcConfigMap.put(dlcConfig.getName(), dlcConfig);
        }

        return dlcConfigMap;
    }

    /**
     * Parse a given DataLoader element
     * @param element
     * @return
     * @throws ConfigParsingException
     */
    private final static DLCConfig parseDataLoader(Element element)
            throws ConfigParsingException {
        DLCConfig dlcConfig = new DLCConfig();

        // Name
        String name = element.getAttribute("name");
        dlcConfig.setName(name.trim());

        // Interval
        String strInterval = element.getAttribute("interval");
        strInterval = strInterval == null ? "" : strInterval.trim();

        // Cron expression
        String cron = element.getAttribute("cron");
        cron = cron == null ? "" : cron.trim();

        if ("".equals(strInterval) && "".equals(cron)) {
            new ConfigParsingException(
                    "You must specify either interval or cron for a dataloader");
        }

        if ("".equals(strInterval)) {
            dlcConfig.setCron(cron);
        }
        else {
            try {
                dlcConfig.setInterval(Integer.parseInt(strInterval));
            }
            catch (Exception ex) {
                new ConfigParsingException(
                        "Invalid interval specified ({}). An integer value is expected",
                        strInterval);
            }
        }

        // Is enabled
        String strEnabled = element.getAttribute("enabled");
        if ("true".equals(strEnabled)) {
            dlcConfig.setEnabled(true);
        }

        // Configuration path
        String configPath = element.getAttribute("configPath");
        configPath = configPath == null ? "" : configPath.trim();
        if ("".equals(configPath)) {
            new ConfigParsingException("You must specify configPath for a dataloader");
        }
        dlcConfig.setConfigPath(configPath);

        return dlcConfig;
    }

    /**
     * @return the dlcConfigMap
     */
    public final Map<String, DLCConfig> getDlcConfigMap() {
        return dlcConfigMap;
    }

    /**
     * @param name
     * @return
     */
    public final DLCConfig getDlcConfig(String name) {
        return this.dlcConfigMap.get(name);
    }
}
