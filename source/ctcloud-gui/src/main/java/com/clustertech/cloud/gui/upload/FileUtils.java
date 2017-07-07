package com.clustertech.cloud.gui.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.utils.CommandUtil;
import com.clustertech.cloud.gui.utils.StringUtil;
import com.clustertech.cloud.gui.utils.CommandUtil.CommandResult;

public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class);
    private static final int LINE_LENGTH_LIMIT = 102400;

    public static List<FileItem> utilList(String userName, String path, String command) throws Exception {

        CommandResult ret = CommandUtil.transferRunTmpBatch(command, null, userName);
        int exitValue = ret.getExitCode();
        if (exitValue != 0) {
            throw new CTCloudException(ret.getStderr());
        }

        List<FileItem> files = new ArrayList<FileItem>();
        String lines = ret.getStdout();
        BufferedReader reader = new BufferedReader(new StringReader(lines));
        String line = null;
        if (reader != null) {
            while ((line = readLine(reader)) != null) {
                if (line.startsWith("total") || line.startsWith("?"))
                    continue;

                FileItem f = new FileItem();

                List<String> itemList = new LinkedList<String>();
                String[] items = line.split(" ");

                // ignore all of the empty string
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null && !items[i].equals("")) {
                        itemList.add(items[i]);
                    }
                }
                // get owner
                String fileOwner = itemList.get(2);
                // get group
                String fileGroup = itemList.get(3);
                // get permission
                String fileAttribute = itemList.get(0);
                String fileUserReadWrite = "";
                if (fileAttribute.charAt(1) == 'r') {
                    fileUserReadWrite += "Read,";
                } else {
                    fileUserReadWrite += "-,";
                }
                if (fileAttribute.charAt(2) == 'w') {
                    fileUserReadWrite += "Write,";
                } else {
                    fileUserReadWrite += "-,";
                }
                if (fileAttribute.charAt(3) == 'w') {
                    fileUserReadWrite += "Execute;";
                } else {
                    fileUserReadWrite += "-;";
                }
                // Group
                if (fileAttribute.charAt(4) == 'r') {
                    fileUserReadWrite += "Read,";
                } else {
                    fileUserReadWrite += "-,";
                }
                if (fileAttribute.charAt(5) == 'w') {
                    fileUserReadWrite += "Write,";
                } else {
                    fileUserReadWrite += "-,";
                }
                if (fileAttribute.charAt(6) == 'w') {
                    fileUserReadWrite += "Execute";
                } else {
                    fileUserReadWrite += "-";
                }
                f.setOwner(fileOwner);
                f.setGroup(fileGroup);
                f.setPermission(fileUserReadWrite);

                // get its size
                String size = itemList.get(4);
                f.setSize(Long.parseLong(size));

                // get its type
                if (itemList.get(0).charAt(0) == 'd')
                    f.setFolder(true);

                // get its date
                String[] d = itemList.get(5).split("-");
                String[] m = itemList.get(6).split("\\.")[0].split(":");
                @SuppressWarnings("deprecation")
                Date time = new Date(Integer.parseInt(d[0]) - 1900, Integer.parseInt(d[1]) - 1, Integer.parseInt(d[2]),
                        Integer.parseInt(m[0]), Integer.parseInt(m[1]), Integer.parseInt(m[2]));
                f.setModifyTime(time);

                // get its name
                String name = "";
                int startPos = line.indexOf("\"");
                if (startPos != -1) {
                    int endPos = line.indexOf("\"", startPos + 1);
                    name = line.substring(startPos + 1, endPos);
                    if (name.indexOf("/") != -1)
                        name = substringAfterLast(name, "/");
                }

                // get symbolic link
                if (path != null && itemList.get(0).charAt(0) == 'l') {
                    if (path != null && !path.endsWith(File.separator)) {
                        path += File.separator;
                    }
                    String cmd = "ls -ldQL --time-style=full-iso '" + path + name + "'";
                    List<FileItem> fileItem = null;
                    try {
                        fileItem = utilList(userName, path, cmd);
                    } catch (Exception e) {
                        logger.error(StringUtil.formatErrorLogger(e, "utilList", userName, path, cmd));
                    }
                    if (fileItem != null && fileItem.size() > 0) {
                        f = fileItem.get(0);
                        files.add(f);
                    }
                    continue;
                }

                // get its ext name
                String extName = "";
                if (!f.isFolder() && name.lastIndexOf(".") != -1) {
                    extName = name.substring(name.lastIndexOf(".") + 1);
                }

                // get Absolute path
                f.setAbsolutePath(path + File.separator + name);

                f.setType(f.isFolder() ? "" : extName);
                f.setName(name);
                f.setHost("server");
                files.add(f);
            }
        }
        return files;
    }

    public static boolean isMatched(String name, String ext) {
        if (ext == null || ext.equals(""))
            return true;
        ext = substringAfterLast(ext, ".");
        return name.endsWith("." + ext);
    }

    public static String substringAfterLast(String str, String separator) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (StringUtils.isEmpty(separator)) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == (str.length() - separator.length())) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    public static String readLine(BufferedReader br) throws Exception {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int c;

        try {
            while (true) {
                c = br.read();
                if (c == -1) {
                    if (sb.length() == 0) {
                        return null;
                    }
                    break;
                }
                if (c == '\n' || c == '\r') {
                    break;
                }
                count++;
                if (count > LINE_LENGTH_LIMIT) {
                    throw new CTCloudException(
                            "Invalid input: Invalid readLine. Read more than maximum characters allowed ("
                                    + LINE_LENGTH_LIMIT + ")");
                }
                sb.append((char) c);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new CTCloudException("Invalid input: Invalid readLine. Problem reading from input stream", e);
        }
    }
}
