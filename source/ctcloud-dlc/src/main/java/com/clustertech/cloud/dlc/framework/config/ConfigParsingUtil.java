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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provide common used utilities for parsing configuration files
 */
public final class ConfigParsingUtil {
    /**
     * Get child element from a given element by tag name.
     * @param element
     * @param tagName
     * @return the child element with the given name, or null if not found
     */
    public final static Element getElementByTagName(Element element,
            String tagName) {
        NodeList nList = element.getElementsByTagNameNS("*", tagName);
        if (nList.getLength() > 0
                && nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
            return (Element) nList.item(0);
        }
        return null;
    }
}
