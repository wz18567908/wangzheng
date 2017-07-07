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

import java.util.concurrent.BlockingQueue;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.operation.Acknowledgment;

/**
 * Client channel to send and receive message
 */
public class DefaultObjectChannelHandler extends SimpleChannelHandler {
    protected BlockingQueue<Acknowledgment> answerQ = null;
    protected final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.DLC);

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("Received message from the master: {}.", e.getMessage().toString());
        }

        if (this.answerQ != null) {
            this.answerQ.offer((Acknowledgment) e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.debug("Communication error.", e.getCause());
        Acknowledgment ack = new Acknowledgment();
        ack.setSuccessful(false);
        ack.setCommunicationError(true);
        ack.setMessage(e.getCause() != null ? e.getCause().getMessage() : "Unknown reason");
        if (this.answerQ != null) {
            this.answerQ.offer(ack);
        }
        ctx.getChannel().disconnect().awaitUninterruptibly();
        ctx.getChannel().close().awaitUninterruptibly();
    }

    /**
     * @return the answerQ
     */
    public final BlockingQueue<Acknowledgment> getAnswerQ() {
        return answerQ;
    }

    /**
     * @param answer the answer to set
     */
    public final void setAnswerQ(BlockingQueue<Acknowledgment> answerQ) {
        this.answerQ = answerQ;
    }
}
