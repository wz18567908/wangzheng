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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.MessageHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceManager;
import com.clustertech.cloud.dlc.framework.jdbc.dstools.DBConfigHelper.Action;

public class DBConfig {
    private static Logger logger = LoggerHelper.getInstance().getLogger(
            LoggerType.CLI);
    private static MessageHelper msgHelper = new MessageHelper(
            "com/clustertech/cloud/dlc/framework/jdbc/Resources");
    private static Action action = Action.EDIT;
    private static String dsName = DBConfigHelper.DEF_DS_NAME;

    public static String usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter buffer = new StringWriter();
        formatter.printHelp(new PrintWriter(buffer), 78,
                "dbconfig [-add <data_source_name> | -edit <data_source_name>"
                        + " | -remove <data_source_name>] | [-help]",
                "options:", options, 2, 2, "\n", false);

        return buffer.toString();
    }

    /**
     * @param args
     * the command line arguments first is add, edit, or remove
     * second is the DSName. or if there is only 1 it is the
     * DSName.... if there are none it looks for the default.
     */
    public static void main(String[] args) {
        Options options = DBConfig.createOptions();
        // Create the GnuParser
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = null;

        // Parse the command line arguments
        try {
            cmdLine = parser.parse(options, args);
        }
        catch (ParseException exp) {
            logger.error(exp.getMessage());
            System.out.println(DBConfig.usage(options));
            System.exit(-1);
        }
        checkParameters(cmdLine, options);

        DataSourceManager dsManager = null;
        try {
            DataSourceHelper dsHelper = DataSourceHelper.getInstance();
            dsHelper.initialize(logger);
            dsManager = dsHelper.getDsManager();
        }
        catch (ConfigParsingException ex) {
            System.out.println("Failed to initialize DataSourceHelper.");
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        executeParameter(dsManager);

        try {
            new DBConfigConsole(action, dsName).execute();
        }
        catch (DBConfigException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }

    private static void checkParameters(CommandLine cmdLine, Options options) {
                    /**
         *  There is no parameter: add or update the default data source ReportDB
         *  There is parameters and the style is not validity, print help message.
                    */
        boolean isParamValid;
        if(cmdLine.getArgList().size() > 0) {
            isParamValid = false;
        }
        else {
            isParamValid = true;
        }
        // Print help message.
        if (cmdLine.hasOption(CommandLineOption.HELP.getTag())) {
            System.out.println(DBConfig.usage(options));
            System.exit(0);
        }

        if (cmdLine.hasOption(CommandLineOption.ADD.getTag())) {
            isParamValid = true;
            action = Action.ADD;
            dsName = cmdLine.getOptionValue(CommandLineOption.ADD.getTag());
            if (dsName == null) {
                System.out.println(DBConfig.usage(options));
                System.exit(-1);
            }
        }

        if (cmdLine.hasOption(CommandLineOption.EDIT.getTag())) {
            isParamValid = true;
            action = Action.EDIT;
            dsName = cmdLine.getOptionValue(CommandLineOption.EDIT.getTag());
            if (dsName == null) {
                dsName = DBConfigHelper.DEF_DS_NAME;
            }
        }

        if (cmdLine.hasOption(CommandLineOption.REMOVE.getTag())) {
            isParamValid = true;
            action = Action.REMOVE;
            dsName = cmdLine.getOptionValue(CommandLineOption.REMOVE.getTag());
            if (dsName == null) {
                System.out.println(DBConfig.usage(options));
                System.exit(-1);
            }
        }
        if (!isParamValid) {
            // Input parameter is not validity, show help message
            System.out.println(DBConfig.usage(options));
            System.exit(0);
        }
    }

    private static void executeParameter(DataSourceManager dsManager) {
        switch (action) {
        case ADD:
            if (dsManager.getDataSourceConfig(dsName) != null) {
                System.out.println(msgHelper.getMessage(
                        "db.duplicated.datasource", new Object[] { dsName }));
                System.exit(-1);
            }
            break;

        case REMOVE:
            try {
                dsManager.removeDataSourceConfig(dsName);
                dsManager.writeConfig(AppConfig.getInstance().getProperty(
                        ConfigKey.CONF_DIR));
                System.out.println(msgHelper.getMessage(
                        "dbconfig.remove.ok", new Object[] { dsName }));
                System.exit(0);
            }
            catch (Exception ex) {
                System.out.println(msgHelper.getMessage("dbconfig.remove.failed",
                        new Object[] { dsName, ex.getMessage() }));
                System.exit(-1);
            }
            break;

        default:
            break;
        }
    }

    private static Options createOptions() {
        // create Options object
        Options options = new Options();

        options.addOption(CommandLineOption.ADD.createOption());
        options.addOption(CommandLineOption.EDIT.createOption());
        options.addOption(CommandLineOption.REMOVE.createOption());
        options.addOption(CommandLineOption.HELP.createOption());
        return options;
    }

    private enum CommandLineOption {
        HELP("help", false, false, "Print help information.", null),
        ADD("add", false, true, "Add the specified data source.", "data_source_name"),
        EDIT("edit", false, true, "Edit the specified data source.", "data_source_name"),
        REMOVE("remove", false, true, "Remove the specified data source.", "data_source_name");

        private String tag;
        private boolean hasArg;
        private String description;
        private String argName;
        private boolean required;

        private CommandLineOption(String tag, boolean required, boolean hasArg,
                String description, String argName) {
            this.tag = tag;
            this.required = required;
            this.hasArg = hasArg;
            this.description = description;
            this.argName = argName;
        }

        public Option createOption() {
            Option opt = new Option(this.tag, this.hasArg, this.description);
            opt.setArgName(this.argName);
            opt.setRequired(this.required);
            return opt;
        }

        /**
         * @return the tag
         */
        public String getTag() {
            return tag;
        }
    }
}
