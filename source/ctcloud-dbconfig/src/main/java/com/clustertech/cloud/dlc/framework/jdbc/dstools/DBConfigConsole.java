/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.jdbc.dstools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceConfig;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceManager;
import com.clustertech.cloud.dlc.framework.jdbc.dstools.DBConfigHelper.Action;

public class DBConfigConsole {

    private static MessageHelper msgHelper = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/jdbc/Resources");
    private static Pattern pattern = Pattern.compile("^jdbc:(\\w+):");
    private static int DEF_MAX_CONN = 30;
    private static Logger logger = LoggerHelper.getInstance().getLogger(
            LoggerType.CLI);
    private final static Properties supportedDrivers = new Properties();

    private Action action;
    private String dsName = "";
    private String userID = "";
    private String password = "";

    private String driverClass = "";
    private String url = "";
    private String maxActive = "";
    private String driverPath = "";
    private boolean isDefault = false;

    private List<DriverDefinition> definedDrivers = new ArrayList<DriverDefinition>();

    public DBConfigConsole(Action action, String dsName) {
        this.action = action;
        this.setDsName(dsName);
    }

    private void parseExistingConf() throws DBConfigException {
        Properties drivers = supportedDrivers;
        boolean foundDriver = false;
        for (Map.Entry<Object, Object> entry : drivers.entrySet()) {
            try {
                Class.forName(entry.getKey().toString());
                this.definedDrivers.add(new DriverDefinition(entry.getKey()
                        .toString(), entry.getValue().toString(), foundDriver));
                foundDriver = true;
            }
            catch (ClassNotFoundException ex) {
                logger.warn("Driver could not be found."
                        + entry.getKey().toString());
            }
        }
        if (!foundDriver) {
            throw new DBConfigException(
                    msgHelper.getMessage("datasource.tools.noDriverFound"));
        }

        DataSourceManager dsManager = DataSourceHelper.getInstance()
                .getDsManager();
        DataSourceConfig dsConfig = null;

        if (DBConfigHelper.DEF_DS_NAME.equals(this.dsName)) {
            dsConfig = dsManager.getDefaultDataSourceConfig();
            if (dsConfig != null) {
                this.dsName = dsConfig.getName();
            }
            else {
                action = Action.ADD;// add datasource
                this.dsName = DBConfigHelper.DEF_DS_NAME;
            }
        }
        else {
            dsConfig = dsManager.getDataSourceConfig(this.dsName);
            if (dsConfig == null) {
                action = Action.ADD; // add a new datasource with the given
                                        // name.
            }
        }

        if (dsConfig != null) {
            this.setUserID(dsConfig.getUserName());
            this.setPassword(dsConfig.getPassword());
            this.setDriver(dsConfig.getDriver());
            this.setUrl(dsConfig.getUrl());
            this.setMaxActive(dsConfig.getProperties().getProperty("maxActive"));
            this.isDefault = dsConfig.isDefault();
        }
    }

    private void updateDBConfig(Scanner scanner) {
        printTitle();
        inputUserID(scanner);
        inputPassword();
        int driverIdx = -1;
        driverIdx = inputDriverIdx(scanner);
        inputURL(scanner, driverIdx);
        inputMaxActive(scanner);
    }

    private void printTitle() {
        // Print title
        System.out.println(msgHelper.getMessage("dbconfig.console.title"));
        System.out.println();

        // Print data source name
        System.out.print(msgHelper.getMessage("dbconfig.datasource.name"));
        System.out.print(" ");
        System.out.println(this.dsName);
        System.out.flush();
    }

    private void inputUserID(Scanner scanner) {
        // Print user ID
        System.out.println();
        System.out.print(msgHelper
                .getMessage("dbconfig.datasource.userName"));
        System.out.print(" "
                + (this.userID.length() > 0 ? "[" + this.userID + "] " : ""));
        System.out.flush();
        // Input user ID
        String tmp = scanner.nextLine().trim();
        if (tmp.length() > 0) {
            this.userID = tmp;
        }
    }

    private void inputPassword() {
        System.out.println();
        String tmpInfo = msgHelper
                .getMessage("dbconfig.datasource.password");

        tmpInfo = tmpInfo + " ";
        if (this.password.length() > 0) {
            tmpInfo = tmpInfo + "[";
            for (int i = 0; i < this.password.length(); i++) {
                tmpInfo = tmpInfo + "*";
            }
            tmpInfo = tmpInfo + "] ";
        }
        char[] password = System.console().readPassword(tmpInfo);
        String pass = new String(password);
        if (pass.length() > 0) {
            this.password = pass;
        }
    }

    private int inputDriverIdx(Scanner scanner) {
        int driverIdx = -1;
        while (true) { // Repeat until get a valid option
            // Print JDBC driver
            System.out.println();
            System.out.println(msgHelper
                    .getMessage("dbconfig.jdbc.class.title"));

            int index = 0;
            int defaultDriver = 0;

            for (DriverDefinition driver : this.definedDrivers) {
                System.out.print("  " + index + " - ");
                System.out.print(driver.getClazz());
                System.out.println(driver.isFoundDriver() ? "*" : "");

                if (driver.getClazz().equals(this.driverClass)) {
                    defaultDriver = index;
                }
                index++;
            }

            System.out.print(msgHelper.getMessage("dbconfig.select.driver",
                    new Object[] { "[" + defaultDriver + "] " }));
            System.out.flush();
            // Input JDBC driver
            String tmp = scanner.nextLine().trim();
            if (tmp.length() > 0) { // Parse inputed value
                try {
                    driverIdx = Integer.parseInt(tmp);
                }
                catch (NumberFormatException ex) {
                    // Ignore
                }
            }
            else { // Type the default value
                driverIdx = defaultDriver;
            }

            if (driverIdx < 0 || driverIdx >= this.definedDrivers.size()) {
                // Invalid option
                msgHelper.printLnInfo(msgHelper
                        .getMessage("dbconfig.invalid.input"));
            }
            else {
                this.driverClass = this.definedDrivers.get(driverIdx)
                        .getClazz();
                break;
            }
        }
        return driverIdx;
    }

    private void inputURL(Scanner scanner, int driverIdx) {
        // Check if the current default URL matching the selected database
        // driver If yes, use the default URL
        // Otherwise, use the URL template of the selected database driver
        String tmp;
        String urlTemp = this.definedDrivers.get(driverIdx).getUrl();
        Matcher matcher = pattern.matcher(urlTemp);
        String dbTemp = matcher.find() ? matcher.group(1) : "";

        matcher = pattern.matcher(this.url);
        String dbDefault = matcher.find() ? matcher.group(1) : "";

        tmp = dbDefault.equals(dbTemp) ? "[" + this.url
                + "]" : "(" + urlTemp + ")";

        // Print URL
        System.out.println();
        System.out.print(msgHelper.getMessage("dbconfig.url.title",
                new Object[] { tmp }));
        System.out.flush();
        // Input URL
        tmp = scanner.nextLine().trim();
        if (tmp.length() > 0) {
            this.url = tmp;
            if (urlTemp.length() == 0) { // No URL template, update the URL template
                this.definedDrivers.get(driverIdx).setUrl(tmp);
            }
        }
    }

    private void inputMaxActive(Scanner scanner) {
        String tmp;
        while (true) {
            // Maximum connections
            String defMaxActive = this.maxActive.length() > 0 ? this.maxActive
                    : String.valueOf(DEF_MAX_CONN);
            System.out.println();
            System.out.print(msgHelper.getMessage("dbconfig.conn.max",
                    new Object[] { defMaxActive }));

            int tmpMaxConn = -1;
            tmp = scanner.nextLine().trim();
            if (tmp.length() > 0) {
                try {
                    tmpMaxConn = Integer.parseInt(tmp);
                } catch (NumberFormatException ex) {
                    // Ignore
                }
            } else {
                try {
                    tmpMaxConn = Integer.parseInt(defMaxActive);
                } catch (NumberFormatException ex) {
                    // Ignore
                }
            }

            if (tmpMaxConn <= 0 || tmpMaxConn > 10000) {
                System.out.println(msgHelper.getMessage("dbconfig.MaxConnNum"));
            } else {
                this.maxActive = String.valueOf(tmpMaxConn);
                break;
            }
        }
    }

    public void execute() throws DBConfigException {
        Scanner scanner = new Scanner(System.in);

        try {
            this.parseExistingConf();
            boolean save = testDBConfig(scanner);
            if (!save) { // Operation canceled
                msgHelper.printLnInfo(msgHelper.getMessage("dbconfig.cancel"));
                return;
            }
            saveDBConfig(scanner);
        }
        finally {
            scanner.close();
            scanner = null;
        }
    }

    private boolean testDBConfig(Scanner scanner) {
        FinishOption finishOption = FinishOption.REDISPLAY;
        boolean done = false;
        boolean save = true;
        while (!done && this.action != Action.REMOVE) {
            // Get inputs
            switch (finishOption) {
            case REDISPLAY:
                this.updateDBConfig(scanner);
                break;

            case TEST:
                try {
                    DBConfigHelper.testInputs(dsName, userID, password,
                            driverClass, url, this.driverPath);
                    msgHelper.printLnInfo(msgHelper
                            .getMessage("dbconfig.test.ok"));
                }
                catch (Exception ex) {
                    msgHelper.printLnInfo(msgHelper.getMessage(
                            "dbconfig.test.failed",
                            new Object[] { ex.getMessage() }));
                    logger.error(msgHelper.getMessage(
                            "dbconfig.test.failed", ""), ex);
                }
                break;

            case SAVE:
                // Save and exit
                done = true;
                save = true;
                break;

            case CANCEL:
                // Cancel
                done = true;
                save = false;

            default:
                break;
            }

            if (!done) {
                // Print Next
                System.out.println();
                System.out.print(msgHelper
                        .getMessage("dbconfig.saveAndExit"));
                System.out.flush();
                // Select
                String tmp = scanner.nextLine().trim();

                    if (tmp.length() > 0) {
                        finishOption = FinishOption.valueOf(Integer
                                .parseInt(tmp));
                    }
                    else {
                        finishOption = FinishOption.TEST;
                    }

                if (finishOption == FinishOption.WRONG_INPUT) {
                    msgHelper.printLnInfo(msgHelper
                            .getMessage("dbconfig.invalid.input"));
                }
            }
        }
        return save;
    }

    private void saveDBConfig(Scanner scanner) {
        boolean done = false;
        while (!done) { // Retry implementation
            try {
                DBConfigHelper.editDataSource(this.dsName, this.userID,
                        this.password, this.driverClass, this.url,
                        this.maxActive, this.isDefault);
                msgHelper.printLnInfo(msgHelper
                        .getMessage("dbconfig.save.ok"));
                break; // Operation finished successfully.
            }
            catch (Exception ex) {
                msgHelper.printLnInfo(msgHelper.getMessage(
                        "dbconfig.save.failed", ex.getMessage()));
            }

            while (true) {
                // Print Next
                System.out.println();
                System.out.print(msgHelper.getMessage("dbconfig.retry"));
                System.out.flush();
                // Select
                String tmp = scanner.nextLine().trim();
                RetryOption retryOption = RetryOption.WRONG_INPUT;
                if (tmp.length() > 0) {
                    retryOption = RetryOption
                            .valueOf(Integer.parseInt(tmp));
                } else {
                    retryOption = RetryOption.RETRY;
                }

                if (retryOption == RetryOption.WRONG_INPUT) {
                    // Invalid input
                    msgHelper.printLnInfo(msgHelper
                            .getMessage("dbconfig.invalid.input"));
                }
                else if (retryOption == RetryOption.CANCEL) {
                    // Canceled
                    done = true;
                    break;
                }
                else { // Retry
                    break;
                }
            }
        }
    }

    /**
     * @param dsName
     *            the dsName to set
     */
    public void setDsName(String dsName) {
        this.dsName = dsName == null ? "" : dsName;
    }

    /**
     * @param userID
     *            the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID == null ? "" : userID;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    /**
     * @param driver
     *            the driver to set
     */
    public void setDriver(String driver) {
        this.driverClass = driver == null ? "" : driver;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url == null ? "" : url;
    }

    /**
     * @param maxActive
     *            the maxActive to set
     */
    public void setMaxActive(String maxActive) {
        this.maxActive = maxActive == null ? "" : maxActive;
    }

    protected enum FinishOption {
        WRONG_INPUT, TEST, SAVE, REDISPLAY, CANCEL;

        public static FinishOption valueOf(int option) {
            FinishOption value;
            try {
                value = FinishOption.values()[option];
            }
            catch (Exception ex) {
                value = WRONG_INPUT;
            }
            return value;
        }
    }

    protected enum RetryOption {
        WRONG_INPUT, RETRY, CANCEL;

        public static RetryOption valueOf(int option) {
            RetryOption value;
            try {
                value = RetryOption.values()[option];
            }
            catch (Exception ex) {
                value = WRONG_INPUT;
            }
            return value;
        }
    }

    protected class DriverDefinition {
        private String clazz;
        private String url;
        private boolean foundDriver;

        public DriverDefinition(String clazz, String url, boolean foundDriver) {
            this.setClazz(clazz);
            this.setUrl(url);
            this.foundDriver = foundDriver;
        }

        /**
         * @return the clazz
         */
        public String getClazz() {
            return clazz;
        }

        /**
         * @param clazz
         *            the clazz to set
         */
        public void setClazz(String clazz) {
            this.clazz = clazz == null ? "" : clazz;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @param url
         *            the url to set
         */
        public void setUrl(String url) {
            this.url = url == null ? "" : url;
        }

        /**
         * @return the foundDriver
         */
        public boolean isFoundDriver() {
            return foundDriver;
        }

        /**
         * @param foundDriver
         *            the foundDriver to set
         */
        public void setFoundDriver(boolean foundDriver) {
            this.foundDriver = foundDriver;
        }
    }

    static {
        supportedDrivers.put("org.gjt.mm.mysql.Driver",
                "jdbc:mysql://<host>[:<port>]/<databaseName>");
        supportedDrivers.put("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@//<host>[:<port>]/SID");
        supportedDrivers.put("org.postgresql.Driver",
                "jdbc:postgresql://<host>:[:<port>]/databaseName");
    }
}
