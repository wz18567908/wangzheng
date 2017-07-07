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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide utilities to manipulate strings
 */
public final class Utils {

    private final static Pattern intervalPattern = Pattern
            .compile("((\\d+) ?[Dd] ?)?((\\d+) ?[Hh] ?)?((\\d+) ?[Mm] ?)?((\\d+) ?([Ss]|$))?$");

    public final static String formatInterval(long interval) {
        StringBuffer result = new StringBuffer();

        long days = interval / 86400;
        interval %= 86400;
        long hours = interval / 3600;
        interval %= 3600;
        long minutes = interval / 60;
        interval %= 60;
        long seconds = interval;

        if (days > 0) {
            result.append(days).append("d");
        }

        if (hours > 0) {
            result.append(hours).append("h");
        }

        if (minutes > 0) {
            result.append(minutes).append("m");
        }

        if (seconds > 0) {
            result.append(seconds).append("s");
        }

        return result.toString();
    }

    /**
     * Parse a given interval expression
     * @param expression
     * @return interval in seconds
     */
    public final static long parseInterval(String expression) {
        long interval = 0;

        Matcher matcher = intervalPattern.matcher(expression);
        if (matcher.find()) {
            if (matcher.group(2) != null) {
                interval += 24 * 3600 * Long.parseLong(matcher.group(2));
            }

            if (matcher.group(4) != null) {
                interval += 3600 * Long.parseLong(matcher.group(4));
            }

            if (matcher.group(6) != null) {
                interval += 60 * Long.parseLong(matcher.group(6));
            }

            if (matcher.group(8) != null) {
                interval += Long.parseLong(matcher.group(8));
            }
        }
        return interval;
    }
}
