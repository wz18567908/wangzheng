package com.clustertech.cloud.gui.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.utils.StringUtil;

public final class ExecuteCLITool implements ICLITimeout {

    private static final Logger logger = Logger.getLogger(ExecuteCLITool.class);

    private IStdIOReader stdIOReader = null;

    private IStdIOReader stdxReader = null;

    private Process process = null;

    private ThreadGroup threadGroup = null;

    public ExecuteCLITool() {
        this.threadGroup = new ThreadGroup("threadGroup");
    }

    public int executeCLI(String cmd, String[] env, long timeOut) throws IOException {
        String[] cmdArray = { cmd };
        return executeCLI(cmdArray, null, timeOut);
    }

    public int executeCLI(String[] cmd, String[] env, long timeOut) throws IOException {
        int result = -1;
        process = Runtime.getRuntime().exec(cmd, env);

        InputStream is = null;
        InputStream iserr = null;
        Thread threadIO = null;
        Thread threadX = null;
        try {

            if (this.stdIOReader != null) {
                is = this.process.getInputStream();
                StdIOReader read = new StdIOReader(this.stdIOReader, is);
                threadIO = new Thread(this.threadGroup, read);
                if (threadIO != null)
                    threadIO.start();
            }

            if (this.stdxReader != null) {
                iserr = this.process.getErrorStream();
                threadX = new Thread(this.threadGroup, new StdIOReader(this.stdxReader, iserr));
                if (threadX != null)
                    threadX.start();
            }

            Thread timeCounter = null;
            if (timeOut > 0) {
                timeCounter = new Thread(new TimeCounter(this, timeOut));
                timeCounter.start();
            }

            try {
                if (this.process != null) {
                    result = this.process.waitFor();
                }
            } catch (InterruptedException ex) {
            }

            try {
                if (threadIO != null)
                    threadIO.join();
            } catch (InterruptedException ex) {
                // Ignore
            }

            try {
                if (threadX != null)
                    threadX.join();
            } catch (InterruptedException ex) {
                // Ignore
            }

            if (timeCounter != null) {
                timeCounter.interrupt();
                timeCounter = null;
            }
        } finally {
            try {
                if (this.process != null) {
                    this.process.destroy();
                    this.process = null;
                }
            } catch (Exception ex) {
                logger.error(StringUtil.formatErrorLogger(ex, "executeCLI", process.toString()));
            }

            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (Exception ex) {
                logger.error(StringUtil.formatErrorLogger(ex, "executeCLI", is.toString()));
            }
        }
        return result;
    }

    public InputStream execCLIWithOut(String[] cmd, String[] envp) throws IOException {
        InputStream stream = null;
        this.process = Runtime.getRuntime().exec(cmd, envp);
        stream = this.process.getInputStream();
        return stream;
    }

    @Override
    public void triggerTimeout(long timeOut) {
        if (this.process != null) {
            this.process.destroy();
            this.process = null;

            InputStream stderr = null;
            Thread errorReader = null;
            String errMsg = "Reach time out limit of " + timeOut + " milliseconds.";
            ByteArrayInputStream errMsgInput = new ByteArrayInputStream(errMsg.getBytes());
            stderr = errMsgInput;
            errorReader = new Thread(this.threadGroup, new StdIOReader(this.stdIOReader, stderr));
            if (errorReader != null)
                errorReader.start();
            this.threadGroup.interrupt();
            this.notifyAll();

        }

    }

    public static String[] splitCmd(String cmd) {
        List<String> cmdList = new ArrayList<String>();
        int len = cmd.length();
        int head = 0;
        int quotationCount = 0;
        int index = 0;
        while (index < len) {
            char ch = cmd.charAt(index);
            if (ch == '"') {
                quotationCount++;
                index++;
                continue;
            } else if (ch == ' ') {
                if (quotationCount % 2 == 0) {
                    cmdList.add(cmd.substring(head, index));
                    index++;

                    // Eat whitespace
                    while (index < len) {
                        if (cmd.charAt(index) == ' ') {
                            index++;
                        } else {
                            break;
                        }
                    }
                    head = index;
                    continue;
                }
            }
            index++;
        }

        if (head < index) {
            cmdList.add(cmd.substring(head, index));
        }
        return cmdList.toArray(new String[cmdList.size()]);
    }

    public IStdIOReader getStdIOReader() {
        return stdIOReader;
    }

    public void setStdIOReader(IStdIOReader stdIOReader) {
        this.stdIOReader = stdIOReader;
    }

    public IStdIOReader getStdxReader() {
        return stdxReader;
    }

    public void setStdxReader(IStdIOReader stdxReader) {
        this.stdxReader = stdxReader;
    }

    private class TimeCounter implements Runnable {
        private long timeOut;

        private ICLITimeout timeoutListener;

        public TimeCounter(ICLITimeout timeoutListener, long timeOut) {
            this.timeoutListener = timeoutListener;
            this.timeOut = timeOut;
        }

        public void run() {
            try {
                Thread.sleep(this.timeOut);
                synchronized (timeoutListener) {
                    this.timeoutListener.triggerTimeout(this.timeOut);
                }
            } catch (InterruptedException e) {
                logger.error(StringUtil.formatErrorLogger(e, "run"));
            }
        }
    }

    private class StdIOReader implements Runnable {
        private IStdIOReader reader;

        private InputStream input;

        public StdIOReader(IStdIOReader reader, InputStream input) {
            this.reader = reader;
            this.input = input;
        }

        public void run() {
            try {
                this.reader.read(this.input);
            } catch (IOException ex) {
                logger.error(StringUtil.formatErrorLogger(ex, "run"));
            }
        }
    }

}
