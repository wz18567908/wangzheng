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
import com.clustertech.cloud.dlc.framework.config.SQLParam.SQLParamType;
import com.clustertech.cloud.dlc.framework.config.WriterConfig.ErrorHandler;

/**
 * Provide utilities to parse and retrieve DataLoaderConfigHelper
 */
public final class LoaderConfigHelper {
    private final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
    private static final String configDir = AppConfig.getInstance().getProperty(AppConfig.ConfigKey.CONF_DIR);
    private static LoaderConfigHelper helper = null;
    private final Map<String, LoaderConfig> loaderConfigMap = new HashMap<String, LoaderConfig>();
    private final static int DEF_MAX_BATCH_SIZE = 5000;
    private final static int DEF_MAX_BUFFER_SIZE = 5000;
    private final static int DEF_MAX_LENGTH = 128;

    /**
     * Create an empty instance
     */
    private LoaderConfigHelper() {
    }

    /**
     * Get the instance of the ServiceConfigHelper
     * @return
     */
    public synchronized final static LoaderConfigHelper getInstance() {
        if (helper == null) {
            helper = new LoaderConfigHelper();
        }
        return helper;
    }

    /**
     * Get the LoaderConfig map
     * @return the loaderConfigMap
     */
    public final Map<String, LoaderConfig> getLoaderConfigMap() {
        return loaderConfigMap;
    }

    /**
     * Get LoaderConfig by a given name
     * @param name
     * @return
     */
    public final LoaderConfig getLoaderConfig(String name) {
        return this.loaderConfigMap.get(name);
    }

    /**
     * Load all configuration files
     * @return
     * @throws ConfigParsingException
     */
    public synchronized final void load(Map<String, DLCConfig> dlcConfigMap) throws ConfigParsingException {

        StringBuffer strBuffer = new StringBuffer();
        // Loop on all files under the services directory
        for (DLCConfig dlcConfig : dlcConfigMap.values()) {
            File file = new File(configDir + File.separator + dlcConfig.getConfigPath());
            try {
                LoaderConfig dlConfig = loadConfigFromXML(file);
                this.loaderConfigMap.put(dlcConfig.getName(), dlConfig);
            } catch (Throwable th) {
                logger.error("Failed to parse the data loader configuration file {}.", file.getAbsolutePath(), th);
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
    public final static LoaderConfig loadConfigFromXML(File xmlFile)
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
                    + "dataloader.xsd");
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            Source source = new StreamSource(xmlFile);
            validator.validate(source);
            dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(xmlFile);
        } catch (Throwable ex) {
            throw new ConfigParsingException(xmlFile.getAbsolutePath(), ex);
        }

        // Get the root element
        Element rootElement = xmlDoc.getDocumentElement();

        LoaderConfig dlConfig = new LoaderConfig();

        // Parse description
        Element elemDesc = ConfigParsingUtil.getElementByTagName(rootElement, "Description");
        dlConfig.setDescription(elemDesc == null ? "" : elemDesc.getTextContent().trim());

        // Parse Reader
        Element elemReader = ConfigParsingUtil.getElementByTagName(rootElement, "Reader");
        if (elemReader == null) {
            throw new ConfigParsingException(
                    "You must specify a Reader element in the data loader configuration file");
        }
        dlConfig.setReaderConfig(parseReaderConfig(elemReader));

        // Parse Writer
        Element elemWriter = ConfigParsingUtil.getElementByTagName(rootElement, "Writer");
        if (elemWriter == null) {
            throw new ConfigParsingException(
                    "You must specify a Writer element in the data loader configuration file");
        }
        dlConfig.setWriterConfig(parseWriterConfig(elemWriter));
        return dlConfig;
    }

    /**
     * Parse Reader element in the configuration file
     * @param elemReader
     * @return
     * @throws ConfigParsingException
     */
    private final static ReaderConfig parseReaderConfig(Element elemReader)
            throws ConfigParsingException {
        ReaderConfig readerConfig = new ReaderConfig();

        // Class
        String clazz = elemReader.getAttribute("class");
        clazz = clazz == null ? "" : clazz.trim();

        if ("".equals(clazz)) {
            throw new ConfigParsingException("You must specify a class for a Reader");
        }

        readerConfig.setReaderClass(clazz);

        // maxBatchSize
        String strMaxBufferSize = elemReader.getAttribute("maxBufferSize");
        strMaxBufferSize = strMaxBufferSize == null ? "" : strMaxBufferSize.trim();
        int maxBufferSize = DEF_MAX_BUFFER_SIZE;
        if (!"".equals(strMaxBufferSize)) {
            try {
                maxBufferSize = Integer.parseInt(strMaxBufferSize);
                maxBufferSize = maxBufferSize <= 0 ? DEF_MAX_BUFFER_SIZE : maxBufferSize;
            } catch (Exception ex) {
                logger.warn(
                        "Invalid maxBufferSize specified ({}). Defaulting to {}.",
                        strMaxBufferSize, maxBufferSize);
            }
        }
        readerConfig.setMaxBufferSize(maxBufferSize);

        // Properties
        Element elemProperties = ConfigParsingUtil.getElementByTagName(
                elemReader, "Properties");
        if (elemProperties != null) {
            NodeList nodeList = elemProperties.getChildNodes();
            int len = nodeList == null ? 0 : nodeList.getLength();
            for (int index = 0; index < len; index++) {
                Node node = nodeList.item(index);
                if (!(node instanceof Element)) {
                    continue;
                }
                Element elemProp = (Element) node;
                String name = elemProp.getAttribute("name");
                name = name == null ? "" : name.trim();
                if ("".equals(name)) {
                    throw new ConfigParsingException(
                            "You must specify a name for a Property");
                }

                String value = elemProp.getTextContent();
                readerConfig.addProperty(name, value);
            }
        }

        // Extra
        Element elemExtra = ConfigParsingUtil.getElementByTagName(elemReader,
                "Extra");
        if (elemExtra != null) {
            NodeList headersList = elemExtra.getElementsByTagNameNS("*", "Headers");
            if (headersList == null || headersList.getLength() == 0) {
                throw new IllegalArgumentException("No header definition found.");
            } else {
                // Loop for all header elements
                NodeList headerList = ((Element) headersList.item(0))
                        .getElementsByTagNameNS("*", "Header");
                if (headerList == null || headerList.getLength() == 0) {
                    throw new IllegalArgumentException(
                            "No header definition found.");
                }
                int len = headerList.getLength();
                for (int index = 0; index < len; index++) {
                    Element elemHeader = (Element) headerList.item(index);

                    // Get the table name
                    String tableName = elemHeader.getAttribute("table");
                    tableName = tableName == null ? "" : tableName.trim();
                    if (tableName == null || "".equals(tableName)) {
                        logger.warn("Table is not specified for the header definition. Ignore the definition.");
                        continue;
                    }

                    // Get the header
                    String headerStr = elemHeader.getTextContent();
                    headerStr = headerStr == null ? "" : headerStr.trim();
                    String[] header = headerStr.split(",");
                    if (logger.isDebugEnabled()) {
                        logger.debug("The header for the table '{}' is {}", tableName, headerStr);
                    }
                    readerConfig.addHeader(tableName, header);
                }
            }
        }

        return readerConfig;
    }

    /**
     * Parse Reader element in the configuration file
     * @param elemReader
     * @return
     * @throws ConfigParsingException
     */
    private final static WriterConfig parseWriterConfig(Element elemWriter) throws ConfigParsingException {
        WriterConfig writerConfig = new WriterConfig();

        // enableRecovery
        String errorHandler = elemWriter.getAttribute("errorHandler");
        errorHandler = errorHandler == null ? "" : errorHandler.trim();
        if ("exit".equalsIgnoreCase(errorHandler)) {
            writerConfig.setErrorHandler(ErrorHandler.EXIT);
        }
        else if ("dlp".equalsIgnoreCase(errorHandler)) {
            writerConfig.setErrorHandler(ErrorHandler.DLP);
        }
        else {
            writerConfig.setErrorHandler(ErrorHandler.DLP);
            logger.warn("Invalid enableRecovery specified ({}). Defaulting to dlp.", errorHandler);
        }

        // maxBatchSize
        String strMaxBatchSize = elemWriter.getAttribute("maxBatchSize");
        strMaxBatchSize = strMaxBatchSize == null ? "" : strMaxBatchSize.trim();
        int maxBatchSize = DEF_MAX_BATCH_SIZE;
        if (!"".equals(strMaxBatchSize)) {
            try {
                maxBatchSize = Integer.parseInt(strMaxBatchSize);
                maxBatchSize = maxBatchSize <= 0 ? DEF_MAX_BATCH_SIZE : maxBatchSize;
            } catch (Exception ex) {
                logger.warn("Invalid maxBatchSize specified ({}). Defaulting to {}.", strMaxBatchSize, maxBatchSize);
            }
        }
        writerConfig.setMaxBatchSize(maxBatchSize);

        // Loop all the SQL elements
        NodeList nodeList = elemWriter.getElementsByTagNameNS("*", "SQL");
        int len = nodeList == null ? 0 : nodeList.getLength();
        if (len == 0) {
            throw new ConfigParsingException("You must specify a SQL in the data loader configuration file");
        }

        for (int index = 0; index < len; index++) {
            Node node = nodeList.item(index);
            if (node instanceof Element == false) {
                continue;
            }

            SQLConfig sqlConfig = parseSQLConfig((Element) node, maxBatchSize);
            writerConfig.addSqlConfig(sqlConfig);
        }

        return writerConfig;
    }

    /**
     * Parse a given SQL element
     * @param elemSQL
     * @return
     * @throws ConfigParsingException
     */
    private final static SQLConfig parseSQLConfig(Element elemSQL, int maxBatchSize) throws ConfigParsingException {
        SQLConfig sqlConfig = new SQLConfig();

        // Data source
        String ds = elemSQL.getAttribute("ds");
        ds = ds == null ? "" : ds.trim();
        sqlConfig.setDs(ds);

        // Table name
        String table = elemSQL.getAttribute("table");
        table = table == null ? "" : table.trim();
        if ("".equals(table)) {
            throw new ConfigParsingException("You must specify a table for a SQL element");
        }
        sqlConfig.setTable(table);

        // maxBatchSize
        String strMaxBatchSize = elemSQL.getAttribute("maxBatchSize");
        strMaxBatchSize = strMaxBatchSize == null ? "" : strMaxBatchSize.trim();
        if (!"".equals(strMaxBatchSize)) {
            try {
                maxBatchSize = Integer.parseInt(strMaxBatchSize);
                maxBatchSize = maxBatchSize <= 0 ? DEF_MAX_BATCH_SIZE : maxBatchSize;
            } catch (Exception ex) {
                logger.warn("Invalid maxBatchSize specified ({}). Defaulting to {}.", strMaxBatchSize, maxBatchSize);
            }
        }
        sqlConfig.setMaxBatchSize(maxBatchSize);

        // Statement
        Element elemStatement = ConfigParsingUtil.getElementByTagName(elemSQL, "Statement");
        if (elemStatement == null) {
            throw new ConfigParsingException("You must specify a Statement in a SQL element");
        }
        String statement = elemStatement.getTextContent();
        statement = statement == null ? "" : statement.trim();
        if ("".equals(statement)) {
            throw new ConfigParsingException("You must specify a non-empty Statement in a SQL element");
        }
        sqlConfig.setStatement(statement);

        // Loop all the Param items
        Element elemParameters = ConfigParsingUtil.getElementByTagName(elemSQL, "Parameters");
        if (elemParameters != null) {
            NodeList nodeList = elemParameters.getChildNodes();
            int len = nodeList == null ? 0 : nodeList.getLength();
            for (int index = 0; index < len; index++) {
                Node node = nodeList.item(index);
                if (!(node instanceof Element)) {
                    continue;
                }

                // Parse SQLParam
                SQLParam sqlParam = parseParam((Element) node);
                sqlConfig.addParam(sqlParam);
            }
        }

        return sqlConfig;
    }

    /**
     * Parse a given Param element
     * @param elemParam
     * @return
     * @throws ConfigParsingException
     */
    private final static SQLParam parseParam(Element elemParam) throws ConfigParsingException {
        SQLParam param = new SQLParam();

        // Parse source column
        String value = elemParam.getAttribute("value");
        value = value == null ? "" : value.trim();
        if ("".equals(value)) {
            throw new ConfigParsingException("You must specify a value column for a SQL parameter");
        }
        param.setValue(value);

        // Parse type
        String type = elemParam.getAttribute("type");
        type = type == null ? "" : type.trim();
        if (!"".equals(type)) {
            try {
                param.setType(SQLParamType.valueOf(type.toUpperCase()));
            } catch (Exception ex) {
                throw new ConfigParsingException(String.format("Invalid SQL parameter type specified (%s)", type));
            }
        }

        // Parse maximum length
        String strMaxLength = elemParam.getAttribute("maxLength");
        strMaxLength = strMaxLength == null ? "" : strMaxLength.trim();

        int maxLength = DEF_MAX_LENGTH;
        // Max length
        if (!"".equals(strMaxLength)) {
            try {
                maxLength = Integer.parseInt(strMaxLength);
                maxLength = maxLength <= 0 ? DEF_MAX_LENGTH : maxLength;
                param.setMaxLen(maxLength);
            } catch (Exception ex) {
                throw new ConfigParsingException(String.format("Invalid SQL parameter type specified (%s)", type));
            }

        }

        return param;
    }
}
