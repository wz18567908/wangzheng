package com.clustertech.cloud.gui.upload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.utils.StringUtil;

public class CLIOutReader implements IStdIOReader {

    public static final Logger logger = Logger.getLogger(CLIOutReader.class);

    private final static String DEFAULT_CHARSET = "UTF-8";

    private final static int BUFFER_SIZE = 1024;

    private String charset = null;

    private StringBuffer outStrBuffer = new StringBuffer();

    public CLIOutReader() {
    }

    public CLIOutReader(String charset) {
        this.charset = charset == null ? DEFAULT_CHARSET : charset;
    }

    public void read(InputStream input) throws IOException {
        BufferedReader reader = null;
        char[] buff = new char[BUFFER_SIZE];
        int len = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(input, charset));
            while ((len = reader.read(buff)) > 0) {
                this.outStrBuffer.append(buff, 0, len);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (Exception ex) {
                    logger.error(StringUtil.formatErrorLogger(ex, "Fail to close reader"));
                }
            }
        }
    }

    public StringBuffer getOutStrBuffer() {
        return outStrBuffer;
    }

    public void setOutStrBuffer(StringBuffer outStrBuffer) {
        this.outStrBuffer = outStrBuffer;
    }

}
