/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.commons;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message utilities class.
 */
public class MessageHelper {
    /**
     * The Resource Bundle instance.
     */
    private ResourceBundle resources;

    /**
     * Constructor for initialize ResourceBundle. For example, you can load a
     * Resource file through the following method:
     * <p>
     * MessageUtil messageUtil = new
     * MessageUtil("com/clustertech/cloud/dlc/framework/Resources");
     * <p>
     * This method will use com/clustertech/cloud/dlc/framework/Resource.properties
     * resource to bundle.
     * @param name
     *            the resource name
     */
    public MessageHelper(String name) {
        resources = ResourceBundle.getBundle(name);
    }

    /**
     * Get the replaced message. The original message can includes the variable
     * that mark as {var}. This method will using the arguments to replace all
     * of variables.
     * @param key
     *            the key of message
     * @param arguments
     *            the arguments that will replace the variables in original
     *            message
     * @return the replaced message
     */
    public String getMessage(String key, Object... arguments) {
        String ret = null;
        try {
            String pattern = resources.getString(key);
            ret = MessageFormat.format(pattern, arguments);
        }
        catch (Exception ex) {
            LoggerHelper.getInstance().writeTmpLog(String.format(
                    "There is exception in getting properties: %s", key));
        }
        return ret;
    }

    /**
     * Print information new line to System out
     * @param info
     */
    public void printLnInfo(String info) {
        System.out.println();
        System.out.println(info);
        System.out.flush();
    }
}
