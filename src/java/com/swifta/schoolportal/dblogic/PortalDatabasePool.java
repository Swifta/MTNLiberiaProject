/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.swifta.schoolportal.utils.AppValues;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class PortalDatabasePool {

    private Logger logger = Logger.getLogger(PortalDatabasePool.class);

    public PortalDatabasePool() {
    }

    public BoneCP getConnectionPool() throws ClassNotFoundException, SQLException {
        BoneCP connectionPool;
        BoneCPConfig config = new BoneCPConfig();
        Class.forName(PortalDatabase.driver);
        config.setJdbcUrl(PortalDatabase.url);
        config.setUsername(PortalDatabase.username);
        config.setPassword(PortalDatabase.password);
        config.setMinConnectionsPerPartition(AppValues.minPool);
        config.setMaxConnectionsPerPartition(AppValues.maxPool);
        config.setPartitionCount(1);
        connectionPool = new BoneCP(config);
        return connectionPool;
    }
    
    
}
