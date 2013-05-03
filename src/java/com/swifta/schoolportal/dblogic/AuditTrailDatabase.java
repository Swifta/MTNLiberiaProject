/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.AuditTrail;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author princeyekaso
 */
public class AuditTrailDatabase {

    private Logger logger = Logger.getLogger(PortalAdminDatabase.class);

    public List<AuditTrail> getAuditTrails(String actionPerformed, String description, String originatorName, String dateFrom, String dateTo) throws SQLException {
        return getAuditTrailByDate(dateFrom, dateTo);
    }

    public List<AuditTrail> getAuditTrailByDate(String dateFrom, String dateTo) throws SQLException {
        String sqlQuery = "select * from audittrail where name = ?";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        PortalDatabase.source.returnConnection(connection);
        return new ArrayList<AuditTrail>();
    }

    public boolean createAuditTrail(AuditTrail auditTrail) throws SQLException {
        int portalAdminId = getLastID("admins") + 1;
        String sqlQuery = "insert into audittrail values (" + portalAdminId + ",'" + auditTrail.getOriginatorName() + "',now(),'" + auditTrail.getActionPerformed() + "','" + auditTrail.getDescription() + "','" + auditTrail.getOriginatorIpAddress() + "','" + auditTrail.getOriginatorId() + "')";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        PortalDatabase.source.returnConnection(connection);
        return ex;
    }

    private int getLastID(String tableName) throws SQLException {
        String sqlQuery = "select max(id) from " + tableName;
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        int id = 0;
        while (res.next()) {
            id = res.getInt(1);
        }
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return id;
    }
}
