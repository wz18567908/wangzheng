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

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;

/**
 * A base class for client to send message to server
 */
public final class NetworkingHelper {
    /** Channel memory limitation 10M */
    public final static long MAX_CHANNEL_MEM = 1048576;

    /** Connection timeout in milliseconds */
    public final static long CONNECT_TIMEOUT = 3000;

    /** If communication error occurs, how many times to retry */
    public final static int MAX_RETRIES = 3;

    /** Small level size in bytes of object to transfer over network */
    public final static int MAX_OBJECT_SIZE_SMALL = 10240;

    /** Medium level size in bytes of object to transfer over network */
    public final static int MAX_OBJECT_SIZE_MEDIUM = 1048576;

    /** Large level size in bytes of object to transfer over network */
    public final static int MAX_OBJECT_SIZE_LARGE = 10485760;

    /** How long in milliseconds to wait answer before timeout */
    public final static long WAIT_ANSWER_TIMEOUT = CONNECT_TIMEOUT * MAX_RETRIES;

    /** Default master port */
    public final static int DEF_DLC_PORT = 9785;

    /** Thread pool size for listener on master side */
    public final static int DLC_LISTENER_POOL_SIZE = 100;

    /** Interval in milliseconds between every retry */
    public final static int RETRY_INTERVAL = 1000;

    private NetworkingHelper() {
        // Prevent from creating instance outside
    }

    /**
     * @param operation
     * @throws InterruptedException
     */
    public final static Acknowledgment send(final String targetHost, final int port,
            final DefaultObjectChannelHandler handler, final int maxObjectSize) {
        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("connectTimeoutMillis", CONNECT_TIMEOUT);

        final ArrayBlockingQueue<Acknowledgment> answerQ = new ArrayBlockingQueue<Acknowledgment>(1);
        handler.setAnswerQ(answerQ);

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder",
                        new ObjectDecoder(maxObjectSize, ClassResolvers.weakCachingConcurrentResolver(null)));
                pipeline.addLast("encoder", new ObjectEncoder());
                pipeline.addLast("handler", handler);
                return pipeline;
            }
        });

        Acknowledgment ack = null;
        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            // Start the connection attempt.
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetHost, port));
            try {
                ack = answerQ.poll(WAIT_ANSWER_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
            }

            if (ack != null) {
                ack.setSuccessful(true);
            } else {
                ack = new Acknowledgment(true);
            }

            // Wait until the connection is closed or the connection attempt fails.
            future.getChannel().disconnect();
            future.getChannel().close();

            // If it was not communication error, no retry is required.
            if (!ack.isCommunicationError()) {
                break;
            }

            if (retries < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_INTERVAL);
                }
                catch (InterruptedException ex) {
                    // Do nothing
                }
            }
        }

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();
        return ack;
    }
}
