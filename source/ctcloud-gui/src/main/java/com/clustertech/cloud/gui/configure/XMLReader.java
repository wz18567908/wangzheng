package com.clustertech.cloud.gui.configure;

import java.io.FileReader;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

import com.clustertech.cloud.gui.exception.CTCloudException;
import com.clustertech.cloud.gui.utils.StringUtil;

public class XMLReader {
    private final Mapping mapping = new Mapping();
    private String xmlFile;

    public XMLReader(String xmlMappingFile, String xmlFile) throws CTCloudException {
        this.xmlFile = xmlFile;
        loadMapping(xmlMappingFile);
    }

    private void loadMapping(String xmlMappingFile) throws CTCloudException {
        try {
            mapping.loadMapping(xmlMappingFile);
        } catch (Exception e) {
            String message = String.format("Faild to load mapping file '%s' due to %s",
                    xmlMappingFile, StringUtil.getStackTrace(e));
            throw new CTCloudException(message);
        }
    }

    public Object getConfigData() throws CTCloudException {
        FileReader fileReader = null;
        Object object = null;

        try {
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            object = unmarshaller.unmarshal(new FileReader(xmlFile));
            if (object == null) {
                throw new CTCloudException("The return type of object is null in getConfigData() function.");
            }
        } catch (Exception e) {
            String message = String.format("Faild to get configure data due to %s", StringUtil.getStackTrace(e));
            throw new CTCloudException(message);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (Exception e) {
                    throw new CTCloudException(e.getMessage());
                }
            }
        }
        return object;
    }
}
