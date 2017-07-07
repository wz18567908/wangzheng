/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.jdbc;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.DesEncrypter;
import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.jdbc.dstools.DBConfigException;

/**
 * The DataSource Manager class. This class provide some method to get a
 * DataSource instance. The invoker can get Connection through returned
 * DataSource instance.
 */
public class DataSourceManager {

    /**
     * A Logger instance.
     */
    private Logger logger = null;

    /**
     * The DataSource instances collection. The key is datasource name, value is
     * DataSource instance.
     */
    private final Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

    /**
     * The DataSource instances collection. The key is datasource driver, value
     * is DataSourceConfig.
     */
    private final Map<String, DataSourceConfig> dsConfigs = new LinkedHashMap<String, DataSourceConfig>();

    /**
     * The DataSource instances collection. The key is datasource driver, value
     * is DataSourceType.
     */
    private final Map<String, DataSourceType> dsTypes = new HashMap<String, DataSourceType>();

    /**
     * set default max Wait to 10*60*1000, it is in milliseconds. The maximum
     * number of milliseconds that the pool will wait (when there are no
     * available connections) for a connection to be returned before throwing an
     * exception, or -1 to wait indefinitely.
     */
    private final long DEF_MAX_WAIT = 10 * 60 * 1000;

    /**
     * The name of Default DataSource.
     */
    private String defaultDataSourceName = null;

    /**
     * The default DataSource instance.
     */
    private DataSource defaultDataSource = null;

    private final static MessageHelper msgHelper = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/jdbc/Resources");
    // DB connection timeout in second
    private final static int DEF_DB_CONN_TIMEOUT = 15;
    private final static int DEF_MAX_ACTIVE = 100;
    private final static int DEF_MIN_ACTIVE = 10;
    private final long DEF_IDLE_TIME = 60 * 60 * 1000;
    public final static String DS_CONF_FILE = "datasource.xml";

    /**
     * Private Constructor, it is to read datasource.xml file and create
     * DataSource instances.
     */
    public DataSourceManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * Configure self instance with given dir, this dir includes the
     * datasource.xml file.
     * @param dir
     *            the datasource configuration file dir.
     * @return current instance.
     * @throws ConfigParsingException
     *             if some exception occured, throw this.
     */
    public DataSourceManager configure(String dir) throws ConfigParsingException {
        String file = dir + File.separator + DS_CONF_FILE;
        return configureByFile(file);
    }

    /**
     * Congigure self instance with given xml configuration file.
     * @param file
     *            the congiruation file of DataSource
     * @return current instance
     * @throws Exception
     *             if some exception occured, throw this.
     */
    private DataSourceManager configureByFile(String file)
            throws ConfigParsingException {
        DataSourceType[] types = DataSourceType.values();
        try {
            NodeList dsList = this.getDataSourceList(file);
            int length = dsList.getLength();
            for (int dsIdx = 0; dsIdx < length; dsIdx++) {

                Element elemDataSource = (Element) dsList.item(dsIdx);
                String name = elemDataSource.getAttribute("name");
                String driver = elemDataSource.getAttribute("driver");
                String deflt = elemDataSource.getAttribute("default");

                Properties props = this.putPropertiesToDSConfig(elemDataSource,
                        name, deflt, driver);

                // Create the DataSource isntance
                DataSource datasource = BasicDataSourceFactory.createDataSource(props);

                if (datasource != null) {
                    if (datasource instanceof BasicDataSource) {
                        ((BasicDataSource) datasource).addConnectionProperty(
                                "loginTimeout",
                                String.valueOf(DEF_DB_CONN_TIMEOUT));
                    }

                    if (dataSources.containsKey(name)) {
                        logger.error(msgHelper.getMessage(
                                "db.duplicated.datasource", name));
                        continue;
                    }

                    dataSources.put(name, datasource);
                    if (deflt != null && deflt.toUpperCase().equals("TRUE")) {
                        if (this.defaultDataSourceName != null) {
                            logger.error(msgHelper
                                    .getMessage("db.duplicated.default.datasource"));
                            continue;
                        }
                        this.defaultDataSourceName = name;
                        this.defaultDataSource = datasource;
                    }

                    boolean foundDriver = false;
                    for (int j = 0; j < types.length; j++) {
                        if (types[j].driver.equals(driver)) {
                            dsTypes.put(name, types[j]);
                            foundDriver = true;
                            break;
                        }
                    }
                    if (!foundDriver) {
                        logger.warn(String.format(
                                "It does not support the driver %s", driver));
                    }

                }
            }// end for dsIdx
        }
        catch (Exception ex) {
            throw new ConfigParsingException(file, ex);
        }

        // If the default data source is not set, make the first as default
        if (this.defaultDataSourceName == null && this.dsConfigs.size() > 0) {
            for (String key : this.dsConfigs.keySet()) {
                this.defaultDataSourceName = key;
                break;
            }
        }
        return this;
    }

    /**
     * Put properties to data source configuration
     */
    private Properties putPropertiesToDSConfig(Element elemDataSource,
            String name, String deflt, String driver) {
        String url = elemDataSource.getAttribute("url");
        String userName = DesEncrypter.getInstance(logger).decrypt(
                elemDataSource.getAttribute("userName"));
        String password = DesEncrypter.getInstance(logger).decrypt(
                elemDataSource.getAttribute("password"));

        DataSourceConfig dsConfig = new DataSourceConfig();
        dsConfig.setName(name);
        dsConfig.setDriver(driver);
        dsConfig.setUrl(url);
        dsConfig.setUserName(userName);
        dsConfig.setPassword(password);
        dsConfig.setDefault(deflt != null && deflt.toUpperCase().equals("TRUE"));
        dsConfigs.put(name, dsConfig);

        // set properties
        NodeList listProps = elemDataSource.getElementsByTagName("Properties");
        if (listProps != null && listProps.getLength() > 0) {
            NodeList listProp = ((Element) listProps.item(0))
                    .getElementsByTagName("Property");

            int propLen = listProp.getLength();
            for (int propIdx = 0; propIdx < propLen; propIdx++) {
                Element elemProp = (Element) listProp.item(propIdx);

                String propName = elemProp.getAttribute("name");
                String propValue = elemProp.getTextContent();
                dsConfig.addProperty(propName, propValue);
            }
        }

        Properties props = new Properties();
        props.putAll(dsConfig.getProperties());

        //The maximum number of active connections that can be allocated
        // from this pool at the same time, or negative for no limit
        String strMaxActive = props.getProperty("maxActive");
        if (strMaxActive == null) {
            props.put("maxActive", String.valueOf(DEF_MAX_ACTIVE));
        }
        else {
            int maxActive = DEF_MAX_ACTIVE;
            try {
                maxActive = Integer.parseInt(strMaxActive.trim());
            }
            catch (Exception ex) {
                logger.warn(String
                        .format("The value of maxActive %s is "
                                + "invalid and it is overridden by "
                                + "the default maximum active value %s ",
                                strMaxActive, DEF_MAX_ACTIVE));
            }

            if (maxActive < DEF_MIN_ACTIVE) {
                logger.warn(String
                        .format("The value of maxActive %s is "
                                + "smaller than minimum allowable value "
                                + "and it is overridden by the default "
                                + "minimum active value %s ", maxActive,
                                DEF_MIN_ACTIVE));
                maxActive = DEF_MIN_ACTIVE;
            }
            props.put("maxActive", String.valueOf(maxActive));
        }

        if (!props.containsKey("maxWait")) {
            props.put("maxWait", String.valueOf(DEF_MAX_WAIT));
        }

        //The minimum number of connections that can remain idle in the pool,
        // without extra ones being created, or zero to create none.
        if (!props.containsKey("minIdle")) {
            props.put("minIdle", 1);
        }

        //The maximum number of connections that can remain idle in the pool,
        // without extra ones being released, or negative for no limit.
        if (!props.containsKey("maxIdle")) {
            props.put("maxIdle", 5);
        }

        // Add the DBCP parameter for handle mysql idle connection issue
        if (!props.containsKey("timeBetweenEvictionRunsMillis")) {
            props.put("timeBetweenEvictionRunsMillis", String.valueOf(DEF_IDLE_TIME));
        }
        if (!props.containsKey("numTestsPerEvictionRun")) {
            props.put("numTestsPerEvictionRun", strMaxActive.trim());
        }
        if (!props.containsKey("minEvictableIdleTimeMillis")) {
            props.put("minEvictableIdleTimeMillis", String.valueOf(DEF_IDLE_TIME));
        }
        props.setProperty("username", userName);
        props.setProperty("password", password);
        props.setProperty("driverClassName", driver);
        props.setProperty("url", url);

        return props;
    }

    /**
     * Get the data source node list from datasource.xml
     */
    private NodeList getDataSourceList(String file)
            throws ConfigParsingException {
        NodeList dsList = null;
        try {
            // Parse the given XML file to DOM
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = null;
            Document xmlDoc = null;

            File confFile = new File(file);
            dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(confFile);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(new File(confFile.getParent()
                    + File.separator + "datasource.xsd"));
            Schema schema = factory.newSchema(schemaFile);

            // create a validator instance, which can be used to validate an
            // instance document
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(xmlDoc), null);

            Element elemRoot = xmlDoc.getDocumentElement();
            dsList = elemRoot.getElementsByTagName("DataSource");
        }
        catch(Exception ex) {
            throw new ConfigParsingException(file, ex);
        }
        return dsList;
    }

    /**
     * Get an instance of DataSource with given name. This method will return
     * the Default DataSource instance if the name is null or "". If can not
     * find the datasource named with given name, return null.
     * @param name
     *            the name of DataSource.
     * @return the DataSource instance otherwise null.
     */
    public DataSource getDataSource(String name) {
        if (name == null || "".equals(name) == true) {
            return this.defaultDataSource;
        }
        return this.dataSources.get(name);
    }

    /**
     * Get an instance of DataSource with given properties of DataSource. If
     * cannot create, return null.
     * @param username
     *            the username of datasource
     * @param password
     *            the password of datasource
     * @param driver
     *            the driver of datasource
     * @param url
     *            the url of datasource
     * @return an instance of DataSource.
     **/
    public DataSource getDataSource(String username, String password,
            String driver, String url) {
        // Create properties instance\
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        props.setProperty("driverClassName", driver);
        props.setProperty("url", url);
        // Create the DataSource isntance
        DataSource datasource = null;
        try {
            datasource = BasicDataSourceFactory.createDataSource(props);
        }
        catch (Exception ex) {
            logger.error(msgHelper.getMessage("db.datasource.initialize.error"), ex);
        }
        return datasource;
    }

    /**
     * Get an instance of DataSourceConfig with given name. This method will
     * return the Default DataSource instance if the name is null or "". If can
     * not find the datasource named with given name, return null.
     * @param name
     *            the name of DataSource.
     * @return the DataSourceConfig instance otherwise null.
     */
    public DataSourceConfig getDataSourceConfig(String name) {
        return this.dsConfigs.get(name);
    }

    /**
     * @return
     */
    public DataSourceConfig getDefaultDataSourceConfig() {
        return this.dsConfigs.get(this.defaultDataSourceName);
    }

    /**
     * Get an DataSourceType with given name. This method will return the
     * Default DataSourceType instance if the name is null or "". If can not
     * find the datasource named with given name, return null.
     * @param name
     *            the name of DataSource.
     * @return the DataSource instance otherwise null.
     */
    public DataSourceType getDataSourceType(String name) {
        if (name == null || name.trim().length() == 0) {
            return getDefaultDataSourceType();
        }
        return this.dsTypes.get(name);
    }

    /**
     * Get the default instance of DataSource. If can't find, return null.
     * @return the default instance of DataSource if success, otherwise null.
     */
    public DataSource getDefaultDataSource() {
        return this.defaultDataSource;
    }

    /**
     * @return the dsConfigs
     */
    public final Map<String, DataSourceConfig> getDataSourceConfigs() {
        return dsConfigs;
    }

    /**
     * Get the default DataSourceType. If can't find, return null.
     * @return the default DataSourceType if success, otherwise null.
     */
    public DataSourceType getDefaultDataSourceType() {
        return this.dsTypes.get(this.defaultDataSourceName);
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public void removeDataSourceConfig(String dsName) throws DBConfigException {
        if (this.dsConfigs.containsKey(dsName) == false) {
            throw new IllegalArgumentException(String.format(
                    "The data source '%s' does not exists", dsName));
        }
        if (this.dsConfigs.get(dsName).isDefault()) {
            throw new DBConfigException(String.format(
                    "The default data source '%s' does not remove", dsName));
        }

        this.dsConfigs.remove(dsName);
    }

    public void addDataSourceConfig(DataSourceConfig dsConfig) {
        this.dsConfigs.put(dsConfig.getName(), dsConfig);
    }

    /**
     * Convert a given DataSourceConfig to a XML Element object
     * @param doc
     * @param dsConfig
     * @return
     */
    public Element toElement(Document doc, DataSourceConfig dsConfig) {
        DesEncrypter desEncrypter = DesEncrypter.getInstance(logger);

        Element elemDs = doc.createElement("DataSource");
        elemDs.setAttribute("name", dsConfig.getName());
        elemDs.setAttribute("driver", dsConfig.getDriver());
        elemDs.setAttribute("url", dsConfig.getUrl());
        elemDs.setAttribute("default", dsConfig.isDefault() ? "true" : "false");
        elemDs.setAttribute("userName", desEncrypter.encrypt(dsConfig.getUserName()));
        elemDs.setAttribute("password", desEncrypter.encrypt(dsConfig.getPassword()));

        Properties props = dsConfig.getProperties();
        if (props != null) {
            Element elemProps = doc.createElement("Properties");
            elemDs.appendChild(elemProps);
            for (Entry<Object, Object> entry : props.entrySet()) {
                Element elemProp = doc.createElement("Property");
                elemProps.appendChild(elemProp);
                elemProp.setAttribute("name", entry.getKey().toString());
                elemProp.setTextContent(entry.getValue().toString());
            }
        }

        return elemDs;
    }

    /**
     * Write the DOM object to configuration file
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public void writeConfig(String dir) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

        Document doc = domBuilder.newDocument();
        Element rootElement = doc.createElement("DataSources");
        doc.appendChild(rootElement);
        doc.setXmlStandalone(true);

        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttribute("xsi:schemaLocation", "http://www.clustertech.com/report/2012/datasource datasource.xsd");

        for (DataSourceConfig dsConfig : this.dsConfigs.values()) {
            rootElement.appendChild(toElement(doc, dsConfig));
        }

        javax.xml.transform.TransformerFactory tranFactory = javax.xml.transform.TransformerFactory.newInstance();
        Transformer transformer = tranFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

        Source src = new DOMSource(doc);
        Result dest = new StreamResult(new File(dir + File.separator + DS_CONF_FILE));
        transformer.transform(src, dest);
    }
}
