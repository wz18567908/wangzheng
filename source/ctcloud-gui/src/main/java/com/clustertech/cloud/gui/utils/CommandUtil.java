package com.clustertech.cloud.gui.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustertech.cloud.gui.domain.job.LSFJobEntity;
import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.jni.lsf.domian.LSFJobInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandUtil {
    private static final Logger logger = Logger.getLogger(CommandUtil.class);
    public static final String LINE_SP = System.getProperty("line.separator");
    private static final String TEMP_DIRECTORY = "/tmp";

    private static final class StdIOReader extends Thread {
        private final String type;
        private final InputStream is;
        private final StringBuilder stdout;
        private final StringBuilder stderr;

        public StdIOReader(InputStream is, String type) {
            this.type = type;
            this.is = is;
            this.stdout = new StringBuilder();
            this.stderr = new StringBuilder();
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    if ("stdout".equals(type)) {
                        this.stdout.append(line);
                        this.stdout.append(LINE_SP);
                    } else {
                        this.stderr.append(line);
                        this.stderr.append(LINE_SP);
                    }
                }
            } catch (IOException e) {
                logger.error(StringUtil.formatErrorLogger(e, "run"));
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(StringUtil.formatErrorLogger(e, "is closed failed"));
                }
            }
        }

        public String getStdout() {
            return stdout.toString();
        }

        public String getStderr() {
            return stderr.toString();
        }
    }

    public static final class CommandResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public CommandResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }

    public static CommandResult runCommand(List<String> commands, Map<String, String> custormerEnv)
            throws CTCloudException {
        Process proc = null;
        int exitCode = -1;
        String stdout = null;
        String stderr = null;
        try {
            ProcessBuilder procBuilder = new ProcessBuilder(commands);
            Map<String, String> env = procBuilder.environment();
            if (custormerEnv != null) {
                for (Entry<String, String> envVar : custormerEnv.entrySet()) {
                    env.put(envVar.getKey(), envVar.getValue());
                }
            }
            proc = procBuilder.start();
            StdIOReader stdoutReader = new StdIOReader(proc.getInputStream(), "stdout");
            StdIOReader stderrReader = new StdIOReader(proc.getErrorStream(), "stderr");
            stdoutReader.start();
            stderrReader.start();
            long timeout = 10L;
            if (proc.waitFor(10L, TimeUnit.SECONDS)) {
                exitCode = proc.exitValue();
                stdoutReader.join();
                stderrReader.join();
                stdout = stdoutReader.getStdout();
                stderr = stderrReader.getStderr();
            } else {
                exitCode = -2;
                String message = String.format("Execute command %s failed (timeout=%s)", commands, timeout);
                throw new CTCloudException(message);
            }
        } catch (IOException e) {
            logger.error(StringUtil.formatErrorLogger(e, "runCommand", commands.toString(), custormerEnv.toString()));
            stderr = (stderr == null) ? e.getMessage() : stderr;
        } catch (InterruptedException e) {
            /* Allow thread to exit */
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return new CommandResult(exitCode, stdout, stderr);
    }

    public static CommandResult transferRunCmmand4User(String cmd, String user, Map<String, String> env) {
        String[] command = null;
        CommandResult cmdResult = null;
        try {
            command = new String[6];
            command[0] = "su";
            command[1] = user;
            command[2] = "-s";
            command[3] = "/bin/sh";
            command[4] = "-c";
            command[5] = cmd;
            cmdResult = CommandUtil.runCommand(Arrays.asList(command), env);
        } catch (Exception e) {
            logger.error(StringUtil.formatErrorLogger(e, "transferRunCmmand4User", cmd, user, env.toString()));
        }
        return cmdResult;
    }

    public static void handleErrResult(CommandResult cmdResult, String sysMsg) throws CTCloudException {
        if (cmdResult != null) {
            int exitCode = cmdResult.getExitCode();
            if (exitCode != 0) {
                if (exitCode > 0) {
                    String msg = cmdResult.getStdout();
                    if (cmdResult.getStderr().replaceAll(CommandUtil.LINE_SP, "").length() > 0) {
                        msg = msg + cmdResult.getStderr();
                    }
                    throw new CTCloudException(msg);
                } else {
                    throw new CTCloudException(sysMsg);
                }
            }
        }
    }

    public static CommandResult transferRunTmpBatch(String script, Map<String, String> env, String user)
            throws CTCloudException {
        File temp = null;
        CommandResult r = null;
        BufferedWriter writer = null;
        try {
            temp = File.createTempFile("tmp" + TransferToBatch4LSF.createRandomChar(8), ".sh",
                    new File(TEMP_DIRECTORY));
            writer = new BufferedWriter(new FileWriter(temp));
            StringBuffer wr = new StringBuffer("#!/bin/bash");
            wr.append("\n");
            wr.append("su ");
            wr.append(user);
            wr.append(" -s /bin/sh -c \"");
            wr.append(script.replace('\r', ' '));
            wr.append("\"\n");
            writer.write(wr.toString());
            writer.flush();
            writer.close();
            writer = null;
            String[] cmd = { "sh", temp.getAbsolutePath() };
            r = CommandUtil.runCommand(Arrays.asList(cmd), env);
        } catch (Exception e) {
            r = new CommandResult(-1, "", e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    writer = null;
                } catch (Exception th) {
                    logger.error(StringUtil.formatErrorLogger(th, "transferRunTmpBatch", script, env.toString(), user));
                }
            }
            if (temp != null) {
                temp.delete();
            }
        }
        return r;
    }

    public static LSFJobEntity transferToJob(LSFJobInfo bakJob, String jobAction) {
        LSFJobEntity job = new LSFJobEntity();
        Long jobId = bakJob.getJobId();
        job.setJobId(jobId.intValue());
        job.setArrayIndex(bakJob.getArrayIndexId());
        job.setJobName(bakJob.getJobName());
        job.setUserName(bakJob.getUserName());
        job.setWorkLoadType(transferJobType(bakJob));
        job.setCommand(bakJob.getCommand());
        job.setQueue(bakJob.getQueue());
        job.setClusterName(bakJob.getClusterName());
        job.setSubmitTime(new Date(bakJob.getSubmitTime() * 1000));
        job.setNumProcessors(bakJob.getNumProcessors());
        job.setProjectName(bakJob.getProjectName());
        if (jobAction != null) {
            String cmd = "bjobs " + jobId + " | awk '{print $3}' | sed -n '2p'";
            CommandResult cmdResult = CommandUtil.transferRunCmmand4User(cmd, "root", null);
            if (jobAction.equalsIgnoreCase("kill") && bakJob.getJobStatus() != 32) {
                job.setJobStatus(cmdResult.getStdout());
            } else if (jobAction.equalsIgnoreCase("suspend")
                    && (bakJob.getJobStatus() != 2 || bakJob.getJobStatus() != 8 || bakJob.getJobStatus() != 16)) {
                job.setJobStatus("SUSP");
            } else if (jobAction.equalsIgnoreCase("requeue") && bakJob.getJobStatus() == 32) {
                job.setJobStatus(cmdResult.getStdout());
            } else if (jobAction.equalsIgnoreCase("resume")
                    && (bakJob.getJobStatus() != 2 || bakJob.getJobStatus() != 8 || bakJob.getJobStatus() != 16)) {
                job.setJobStatus(cmdResult.getStdout());
            }
        } else {
            job.setJobStatus(transferStatus(bakJob.getJobStatus()));
        }
        if (bakJob.getStartTime() == 0) {
            job.setStartTime(null);
        } else {
            job.setStartTime(new Date(bakJob.getStartTime() * 1000));
        }
        if (bakJob.getEndTime() == 0) {
            job.setEndTime(null);
        } else {
            job.setEndTime(new Date(bakJob.getEndTime() * 1000));
        }
        return job;
    }

    private static String transferJobType(LSFJobInfo job) {
        String jobType = "JOB";
        if (job.getArrayIndexId() != 0) {
            jobType = "ARRAY";
        }
        return jobType;
    }

    private static String transferStatus(int status) {
        switch (status) {
        case LSFConstants.JOB_STAT_NULL:
            return "NULL";
        case LSFConstants.JOB_STAT_PEND:
            return "PEND";
        case LSFConstants.JOB_STAT_PSUSP:
            return "PSUSP";
        case LSFConstants.JOB_STAT_RUN:
            return "RUN";
        case LSFConstants.JOB_STAT_RUN | LSFConstants.JOB_STAT_WAIT:
            return "PEND";
        case LSFConstants.JOB_STAT_SSUSP:
            return "SSUSP";
        case LSFConstants.JOB_STAT_USUSP:
            return "USUSP";
        case LSFConstants.JOB_STAT_EXIT:
            return "EXIT";
        case LSFConstants.JOB_STAT_DONE:
        case LSFConstants.JOB_STAT_DONE | LSFConstants.JOB_STAT_PDONE:
        case LSFConstants.JOB_STAT_DONE | LSFConstants.JOB_STAT_WAIT:
        case LSFConstants.JOB_STAT_DONE | LSFConstants.JOB_STAT_PERR:
            return "DONE";
        case LSFConstants.JOB_STAT_UNKWN:
            return "EXIT";
        default:
            return "EXIT";
        }
    }

    public static String transferJobAction(String action) {
        String command = "";
        switch (action.toLowerCase()) {
        case LSFConstants.JOB_COMMAND_BRESUME_JOB:
            command = "bresume ";
            break;
        case LSFConstants.JOB_COMMAND_BKILL_JOB:
            command = "bkill ";
            break;
        case LSFConstants.JOB_COMMAND_BSTOP_JOB:
            command = "bstop ";
            break;
        case LSFConstants.JOB_COMMAND_BREQUEUE_JOB:
            command = "brequeue -a  ";
            break;
        case LSFConstants.JOB_COMMAND_BPEEK_JOB:
            command = "bpeek ";
            break;
        default:
        }
        return command;
    }

    public static <T> Object JSONToObj(String jsonStr, Class<T> obj) {
        T t = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            t = objectMapper.readValue(jsonStr, obj);
        } catch (Exception e) {
            logger.error(StringUtil.formatErrorLogger(e, "JSONToObj", jsonStr, obj.toString()));
        }
        return t;
    }

    public String getPastTime(Object interval) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR, -Integer.parseInt(interval.toString()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }
}
