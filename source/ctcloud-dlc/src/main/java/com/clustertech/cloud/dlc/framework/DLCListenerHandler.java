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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;
import com.clustertech.cloud.dlc.framework.operation.OperationBase;
import com.clustertech.cloud.dlc.framework.operation.OperationType;

public class DLCListenerHandler extends SimpleChannelHandler {
    private final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String serverStatus ="alive";
        Object obj = e.getMessage();
        if (logger.isTraceEnabled()) {
            logger.trace("Message received: {}", e.getMessage().toString());
        }

        Acknowledgment ack = null;
        ack = new Acknowledgment();
        ack.setSuccessful(false);
        if (obj instanceof OperationBase
                && ((OperationBase) obj).getOperation() == OperationType.DAEMON_SHUTDOWN) {
            ack.setMessage(String.format("Unexpected message received: %s",
                    obj.toString()));
        }
        if (obj instanceof OperationBase
                && ((OperationBase) obj).getOperation() == OperationType.PING) {
            // Write more acknowledgment back to the cli
            ack.setMessage(serverStatus);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Writing acknowledgment: {}", ack);
        }
        // Write acknowledgment back to the cli
        Channels.write(ctx.getChannel(), ack);

        if (obj instanceof OperationBase
                && ((OperationBase) obj).getOperation() == OperationType.DAEMON_SHUTDOWN) {
            DLController.getInstance().shutdown();
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Exception occurred.", e.getCause());
        e.getChannel().close();
    }
}

