package com.clustertech.cloud.dlc.framework.jdbc.jndi;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.slf4j.Logger;

import com.clustertech.cloud.dlc.framework.commons.ConfigParsingException;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper;
import com.clustertech.cloud.dlc.framework.commons.LoggerHelper.LoggerType;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceHelper;
import com.clustertech.cloud.dlc.framework.jdbc.DataSourceManager;

public class ReportDataSourceFactory implements ObjectFactory {

    protected final static Logger logger = LoggerHelper.getInstance().getLogger(LoggerType.REPORT);
    private DataSourceManager dsManager;
    
    public ReportDataSourceFactory() throws ConfigParsingException {
        DataSourceHelper dsHelper = DataSourceHelper.getInstance();
        dsHelper.initialize(logger);
        dsManager = dsHelper.getDsManager();
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> env) throws Exception {
        String dsName = "";
        Reference ref = (Reference) obj;
        Enumeration<RefAddr> addrs = ref.getAll();
        while (addrs.hasMoreElements()) {
            RefAddr addr = addrs.nextElement();
            String type = addr.getType();
            if (type.equals("dsName")) {
                dsName = (String) addr.getContent();
            }
        }
        logger.info("Use CT Report DataSource " + dsName);
        Object ds = dsManager.getDataSource(dsName);
        logger.info("DataSource looked up: Class: " + ds.getClass().getName() +
                " Hash Code: " + ds.hashCode());
        return ds;
    }

}
