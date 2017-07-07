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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;

/**
 * A factory of SQLExceptionTransformer. The function of this class is get a
 * SQLExceptionTransformer instance with given name.
 */
public class SQLExceptionTransformerFactory {
    private static SQLExceptionTransformerFactory factory = null;
    private Map<String, SQLExceptionTransformer> transformers = null;
    private final static String configFile = "/com/clustertech/cloud/dlc/framework/jdbc/transformer.xml";

    private SQLExceptionTransformerFactory() {
        this.transformers = new LinkedHashMap<String, SQLExceptionTransformer>();
    }

    public static SQLExceptionTransformerFactory getInstance() throws ConfigParsingException {
        if (factory == null) {
            SQLExceptionTransformerFactory tmpFactory = new SQLExceptionTransformerFactory();
            InputStream is = SQLExceptionTransformerFactory.class.getResourceAsStream(configFile);
            tmpFactory.configure(is);
            factory = tmpFactory;
        }

        return factory;
    }

    /**
     * @param is inputstream of the transformer configuration file
     * @throws ConfigParsingException
     */
    @SuppressWarnings("unchecked")
    private void configure(InputStream is) throws ConfigParsingException {
        // Parase the given XML file to DOM
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = null;
        Document xmlDoc = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(is);
            Element elemRoot = xmlDoc.getDocumentElement();

            SQLExceptionTransformer transformer = null;

            NodeList listTransformer = elemRoot.getElementsByTagName("Transformer");
            int transLen = listTransformer.getLength();
            for (int transIdx = 0; transIdx < transLen; transIdx++) {
                Element elemTrans = (Element) listTransformer.item(transIdx);

                String tranName = elemTrans.getAttribute("name");
                String tranClassName = elemTrans.getAttribute("class");

                Class<? extends SQLExceptionTransformer> tranClass = Class
                        .forName(tranClassName).asSubclass(
                                SQLExceptionTransformer.class);
                Constructor<? extends SQLExceptionTransformer> tranConstructor = tranClass
                        .getConstructor();
                transformer = tranConstructor.newInstance();

                Map<String, Class<DataAccessException>> mapClass = new LinkedHashMap<String, Class<DataAccessException>>();
                Map<String, Set<String>> mapProps = new LinkedHashMap<String, Set<String>>();

                NodeList listExec = elemRoot.getElementsByTagName("DataAccessException");
                int execLen = listTransformer.getLength();
                for (int execIdx = 0; execIdx < execLen; execIdx++) {
                    Element elemExec = (Element) listExec.item(execIdx);
                    String exName = elemExec.getAttribute("name");
                    String exClassName = elemExec.getAttribute("class");

                    mapClass.put(exName, (Class<DataAccessException>) Class.forName(exClassName));

                    NodeList listProps = elemExec.getElementsByTagName("Properties");
                    if (listProps != null && listProps.getLength() > 0) {
                        Element elemProps = (Element) listProps.item(0);
                        String props = elemProps.getTextContent();
                        String[] arr = props.split(",");
                        Set<String> list = new HashSet<String>();
                        for (int i = 0; i < arr.length; i++) {
                            list.add(arr[i].trim());
                        }
                        mapProps.put(exName, list);
                    }
                }

                transformer.setMapClass(mapClass);
                transformer.setMapProperties(mapProps);

                this.transformers.put(tranName, transformer);
            }
        }
        catch (Exception ex) {
            throw new ConfigParsingException(configFile, ex);
        }
    }

    /**
     * Get an instance of SQLExceptionTransformer. If can not find this
     * instance, this method will return null.
     * @param name
     *            the name of SQLExceptionTransformer
     * @return an instance of SQLExceptionTransformer, otherwise null
     */
    public SQLExceptionTransformer getTransformer(String name) {
        if (null == name) {
            throw new IllegalArgumentException();
        }
        return this.transformers.get(name);
    }
}
