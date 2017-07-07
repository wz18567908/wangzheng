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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressFileUtil {
    private static final int BUFFER = 10000;

    /**
     * Creates a new instance of CompressFileUtil
     */
    public CompressFileUtil() {
    }

    /**
     * Compress a file to another file.
     * @param from
     *            The fully qualified path+name of the source file.
     * @param to
     *            The fully qualified path+name of the new file.
     * @return
     * @throws IOException
     */
    public void CompressFile(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);
        int bufSize = 0;
        if (toFile.exists()) {
            toFile.delete();
        }
        if (fromFile.length() > BUFFER) {
            bufSize = BUFFER;
        } else {
            bufSize = (int) fromFile.length();
        }
        byte[] buffer = new byte[bufSize];
        FileInputStream in = new FileInputStream(fromFile);
        FileOutputStream dest = new FileOutputStream(toFile);
        ZipOutputStream out = new ZipOutputStream(
                new BufferedOutputStream(dest));
        out.setMethod(ZipOutputStream.DEFLATED);
        ZipEntry entry = new ZipEntry(fromFile.getName());
        out.putNextEntry(entry);
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        in.close();
        out.closeEntry();
        out.finish();
        out.close();
        toFile.setLastModified(fromFile.lastModified());
    }
}
