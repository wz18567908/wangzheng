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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.DefaultObjectChannelHandler;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.NetworkingHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;
import com.clustertech.cloud.dlc.framework.operation.OperationBase;
import com.clustertech.cloud.dlc.framework.operation.OperationType;

/**
 *  Utility for shutdown DLC on the master
 */
public class Daemon {
    protected final static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    // Command line options
    protected Options options = null;
    protected String cmd = null;
    // Command line option, start or stop
    protected CmdOption operation = null;
    protected String hostName = null;
    private static Logger logger = null;

    protected Daemon(Logger logger) {
        if (logger == null) {
            logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
        }
    }

    public static String getVMInfo() {
        return String.format("OS: {%s %s} Processors: {%d} Arch: {%s} VM: {%s %s}.", System.getProperty("os.name"),
                System.getProperty("os.version"), Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.arch"), runtimeBean.getVmName(), runtimeBean.getVmVersion());
    }

    public static String getMemInfo() {
        // Log detailed info
        long uptime = runtimeBean.getUptime() / 1000;

        return String.format("Uptime: %s Avaiable heap size: {%dM}", Utils.formatInterval(uptime),
                (long) (Runtime.getRuntime().freeMemory()) / 1048576L);
    }

    /**
     * Send shutdown command to the given host and port
     * @param hostName
     * @param port
     * @return
     * @throws InterruptedException
     */
    protected Acknowledgment shutdown(String hostName, int port) throws InterruptedException {
        DefaultObjectChannelHandler handler = new DefaultObjectChannelHandler() {
            /* (non-Javadoc)
             * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                Channel channel = e.getChannel();
                if (channel.isWritable()) {
                    OperationBase operation = new OperationBase(OperationType.DAEMON_SHUTDOWN);
                    logger.debug("Writing command {} ... ", operation);

                    Channels.write(channel, operation);
                    logger.debug("done.");
                }
            }
        };
        return NetworkingHelper.send(hostName, port, handler, NetworkingHelper.MAX_OBJECT_SIZE_SMALL);
    }

    /**
     * @param args
     * @throws ParseException 
     */
    protected void parseCommandLine(String[] args) throws ParseException {
        CommandLine cli = null;

        // If the option is not started with "-", correct the option
        String[] cloneArgs = args.clone();
        if (args.length > 0 && args[0].startsWith("-") == false) {
            cloneArgs[0] = "--" + args[0].toUpperCase();
        }

        cli = this.createCommandLine(cloneArgs);

        if (cli.hasOption(CmdOption.STOP.name())) {
            this.operation = CmdOption.STOP;
            this.hostName = cli.getOptionValue(CmdOption.STOP.name());
        }
        else {
            this.operation = CmdOption.START;
        }
    }

    /**
     * Create command-line with given args
     * @param args
     * @return
     * @throws ParseException
     */
    @SuppressWarnings("static-access")
    private final CommandLine createCommandLine(String[] args) throws ParseException {
        this.options = new Options();

        // Start option
        Option start = OptionBuilder.hasArg(false).withDescription("start daemon on the host")
                .create(CmdOption.START.name());
        this.options.addOption(start);

        // stop option
        Option stop = OptionBuilder.hasOptionalArg().withArgName("host_name")
                .withDescription("stop daemon on the given host").create(CmdOption.STOP.name());
        this.options.addOption(stop);
        CommandLineParser parser = new GnuParser();
        CommandLine cli = parser.parse(this.options, args);
        return cli;
    }

    /**
     * Print usage information
     * @param error
     */
    protected final void usage(String cmd, boolean help) {
        StringWriter buffer = new StringWriter();
        // Automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp(new PrintWriter(buffer), 78, cmd + " start | stop", "Options:", options, 2, 2, "\n", false);

        if (help == false) {
            logger.error(buffer.getBuffer().toString());
        } else {
            System.out.println(buffer.getBuffer().toString());
        }
    }

    protected enum CmdOption {
        START, STOP
    }

    /**
     * @return the operation
     */
    public final CmdOption getOperation() {
        return operation;
    }
}
