/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
 */
package com.clustertech.cloud.dlc.framework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.DefaultObjectChannelHandler;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.NetworkingHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.config.AppConfig;
import com.clustertech.cloud.dlc.framework.config.AppConfig.ConfigKey;
import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;
import com.clustertech.cloud.dlc.framework.operation.OperationBase;
import com.clustertech.cloud.dlc.framework.operation.OperationType;

public final class DLCListener {

    private static DLCListener listener = null;
    private final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);
    private String serverStatus="alive";
    private static final String slaveRole ="Slave";
    public  String serverRole="Primary";
    private int port = NetworkingHelper.DEF_DLC_PORT;
    private int listenerThreads = NetworkingHelper.DLC_LISTENER_POOL_SIZE;

    protected long connectTimeout = NetworkingHelper.CONNECT_TIMEOUT; // Connection timeout in milliseconds
    protected Channel parentChannel = null;
    protected OrderedMemoryAwareThreadPoolExecutor pipelineExecutor = null;
    protected NioServerSocketChannelFactory channelFactory = null;

    public DLCListener() {

        // Get the port from configuration
        String strPort = AppConfig.getInstance().getProperty(AppConfig.ConfigKey.DLC_PORT);
        this.port = Integer.parseInt(strPort);

        // Get the max # of thread from configuration
        String strTimeout = AppConfig.getInstance().getProperty("CONNECT_TIMEOUT");

        if (strTimeout != null) {
            logger.debug("CONNECT_TIMEOUT: {{}}", strTimeout);
            try {
                this.connectTimeout = Long.parseLong("strTimeout") * 1000;
            } catch (Exception ex) {
                logger.warn("The CONNECT_TIMEOUT is invalid: {}. Defaulting to {}", new Object[] { strTimeout,
                        this.connectTimeout });
            }
        }
    }

    /**
     * Start the listener
     * @throws IOException
     */
    protected synchronized final void startup(final SimpleChannelHandler handler, final int port,
            final int listenerThreads, final int maxObjectSize) {

        long maxGlobalMemory = (long) (Runtime.getRuntime().maxMemory() * 0.4);
        this.channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.reuseAddress", true);
        bootstrap.setOption("child.connectTimeoutMillis", this.connectTimeout);
        bootstrap.setOption("readWriteFair", true);

        this.pipelineExecutor = new OrderedMemoryAwareThreadPoolExecutor(listenerThreads,
                NetworkingHelper.MAX_CHANNEL_MEM, maxGlobalMemory, listenerThreads / 2, TimeUnit.MILLISECONDS,
                Executors.defaultThreadFactory());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder",
                        new ObjectDecoder(maxObjectSize, ClassResolvers.weakCachingConcurrentResolver(null)));
                pipeline.addLast("encoder", new ObjectEncoder());
                pipeline.addLast("pipelineExecutor", new ExecutionHandler(pipelineExecutor));
                pipeline.addLast("handler", handler);
                return pipeline;
            }
        });
        this.parentChannel = bootstrap.bind(new InetSocketAddress(port));
    }

    public void setServerRole(String role) {
        serverRole=role;
    }

    public String getServerRole() {
       return serverRole;
    }

    /**
     * Check whether the HA Host is available. If it is available,
     * this daemon will not start up.
     * @throws IOException
     */
    public synchronized final void syncUp(String strMasterHost) {
        int port = Integer.parseInt(AppConfig.getInstance().getProperty(ConfigKey.DLC_PORT));
        reConnectServer:
        while(true) {
            try {
                 Acknowledgment ack = pingRemote(strMasterHost, port);
                 if (ack != null && ack.isSuccessful() && ack.getMessage().contains(serverStatus)) {
                       //Localhost changed from server to client, use SERVER_ROLE "slave" as tag.
                        if(this.getServerRole().equalsIgnoreCase(slaveRole)) {
                            this.shutdown();
                            logger.info("The remote host has been start up as server, so localhost will be changed to client.");
                        } else {
                            //Localhost started as client at firstly.
                            logger.info("The remote host has been start up as server, so localhost as client to re-try.");
                        }
                        Thread.sleep(connectTimeout);
                        continue reConnectServer;
                   } else {
                        logger.info("start localhost as slave host.");
                        this.setServerRole(slaveRole);
                        break;
                   }
            } catch (InterruptedException ex) {
                    logger.info("Failed to connect the master due to:", ex);
                    logger.info("Current host will be started as slave host.");
                    this.setServerRole(slaveRole);
                    break;
            }
        }
    }

    /**
     * Send ping command to the given host and port
     * @param hostName
     * @param port
     * @return
     * @throws InterruptedException
     */
    private Acknowledgment pingRemote(String hostName, int port) throws InterruptedException {
        DefaultObjectChannelHandler handler = new DefaultObjectChannelHandler() {
            /* (non-Javadoc)
             * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                Channel channel = e.getChannel();
                if (channel.isWritable()) {
                    OperationBase operation = new OperationBase(OperationType.PING);
                    logger.debug("Writing command {} ... ", operation);

                    Channels.write(channel, operation);
                    logger.debug("done.");
                }
            }
        };
        return NetworkingHelper.send(hostName, port, handler, NetworkingHelper.MAX_OBJECT_SIZE_SMALL);
    }

    /**
     * Shutdown the server
     */
    public synchronized final void shutdown() {
        logger.info("Stopping the listener ... ");
        try {
            if (this.parentChannel != null) {
                this.parentChannel.disconnect().awaitUninterruptibly();
                this.parentChannel.close().awaitUninterruptibly();
                this.parentChannel.unbind().awaitUninterruptibly();
            }
        } catch (Throwable th) {
            // Do nothing
        }

        try {
            if (this.pipelineExecutor != null) {
                this.pipelineExecutor.shutdownNow();
            }
        } catch (Throwable th) {
            // Do nothing
        }

        try {
            if (this.channelFactory != null) {
                this.channelFactory.releaseExternalResources();
            }
        } catch (Throwable th) {
            // Do nothing
        }

        logger.info("done.");
    }

    /**
     * Get an instance of the MasterListener class
     * @return
     */
    public synchronized final static DLCListener getInstance() {
        if (listener == null) {
            listener = new DLCListener();
        }
        return listener;
    }

    public synchronized final void startup() throws IOException {
        logger.info("Starting the master listener (port: {}) ...", port);
        DLCListenerHandler handler = new DLCListenerHandler();
        this.startup(handler, port, listenerThreads, NetworkingHelper.MAX_OBJECT_SIZE_MEDIUM);
        logger.info("done.");
    }
}
