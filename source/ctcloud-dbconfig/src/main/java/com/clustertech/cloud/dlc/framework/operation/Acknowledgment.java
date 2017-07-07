/*
 * Copyright (c) 2000 - 2012 by Cluster Technology Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Cluster Technology, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Cluster Technology.
*/

package com.clustertech.cloud.dlc.framework.operation;

import java.io.Serializable;

/**
 * The Acknowledgment class provide information for acknowledging a communication
 */
public class Acknowledgment implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 110190564033356409L;
    public final static String COMMUNICATION_TIMEOUT = "Communication timeout";

    private boolean successful = true;
    private boolean communicationError = false;
    private String message = "";
    private Serializable additionalInfo = null;
    private Throwable cause = null;

    public Acknowledgment() {
    }

    public Acknowledgment(boolean timeout) {
        if (timeout == true) {
            this.successful = false;
            this.message = "already timed out";
        }
    }

    /**
     * @return the sucessful
     */
    public final boolean isSuccessful() {
        return successful;
    }

    /**
     * @param sucessful the sucessful to set
     */
    public final void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @return the message
     */
    public final String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public final void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the additionalInfo
     */
    public final Serializable getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * @param additionalInfo the additionalInfo to set
     */
    public final void setAdditionalInfo(Serializable additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * @return the communicationError
     */
    public final boolean isCommunicationError() {
        return communicationError;
    }

    /**
     * @param communicationError the communicationError to set
     */
    public final void setCommunicationError(boolean communicationError) {
        this.communicationError = communicationError;
    }

    /**
     * @return the exception
     */
    public final Throwable getCause() {
        return cause;
    }

    /**
     * @param cause the exception to set
     */
    public final void setCause(Throwable cause) {
        this.cause = cause;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[successful=" + successful + ", communicationError=" + communicationError + ", message=" + message
                + ", additionalInfo=" + additionalInfo + ", cause=" + cause + "]";
    }
}
