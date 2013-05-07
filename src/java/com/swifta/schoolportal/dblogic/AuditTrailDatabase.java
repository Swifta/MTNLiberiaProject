/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.AuditTrail;
import com.swifta.schoolportal.utils.AppValues;
import com.swifta.schoolportal.utils.DateUtilities;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author princeyekaso
 */
public class AuditTrailDatabase {

    private Logger logger = Logger.getLogger(PortalAdminDatabase.class);
    private DateUtilities dateUtility = new DateUtilities();
    private AppValues appValues = new AppValues();

    public List<AuditTrail> getAuditTrails(String fromDate, String toDate) throws SQLException {
        return getAuditTrails(null, null, null, fromDate, toDate, null);
    }

    public List<AuditTrail> getAuditTrails(List<String> actionPerformed) throws SQLException {
        return getAuditTrails(actionPerformed, null, null, null, null, null);
    }

    public List<AuditTrail> getAuditTrails(List<String> actionPerformed, String dateFrom, String dateTo) throws SQLException {
        return getAuditTrails(actionPerformed, null, null, dateFrom, dateTo, null);
    }

    public List<AuditTrail> getAuditTrails(List<String> actionPerformed, List<String> actionType) throws SQLException {
        return getAuditTrails(actionPerformed, null, null, null, null, actionType);
    }

    public String formatQuery(String partQuery, String sqlQuery) {
        String formattedQuery = sqlQuery + " and (" + partQuery.substring(0, partQuery.length() - 4) + ")";
        return formattedQuery;
    }

    public List<AuditTrail> getAuditTrails(List<String> actionPerformed, String description, String originatorName, String dateFrom, String dateTo, List<String> actionType) throws SQLException {
        String sqlQuery = "select * from audittrail where date_created <= ? and date_created >= ?";
        String partQuery = "";
        // dateFrom = dateUtility.formatStringDate(dateFrom, appValues.DEFAULT_DATE_FORMAT, false).toString();
        dateTo = dateUtility.incrementByADay(dateTo, appValues.DEFAULT_DATE_FORMAT);
        Iterator<String> defIter = null;
        if (actionPerformed != null && !actionPerformed.isEmpty()) {
            logger.info("action performed not null");
            defIter = actionPerformed.iterator();
            while (defIter.hasNext()) {
                logger.info("----------actionperformed!!!!!!!!!");
                partQuery += "lower(action_performed) = ? or ";
                defIter.next();
            }
            sqlQuery = formatQuery(partQuery, sqlQuery);
        }
        partQuery = "";
        if (actionType != null && !actionType.isEmpty()) {
            logger.info("action type not null");
            defIter = actionType.iterator();
            while (defIter.hasNext()) {
                logger.info("-----------actiontype=======");
                partQuery += "lower(originator_type) = ? or ";
                defIter.next();
            }
            sqlQuery = formatQuery(partQuery, sqlQuery);
        }
        logger.info("------SQL--------" + sqlQuery);
        List<AuditTrail> auditTrails = new ArrayList<AuditTrail>();
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        int count = 2;
        prepStmt.setString(1, dateTo);
        prepStmt.setString(2, dateFrom);
        if (actionPerformed != null) {
            defIter = actionPerformed.iterator();
            while (defIter.hasNext()) {
                prepStmt.setString(++count, defIter.next().toLowerCase());
            }
        }
        if (actionType != null) {
            defIter = actionType.iterator();
            while (defIter.hasNext()) {
                prepStmt.setString(++count, defIter.next().toLowerCase());
            }
        }
        logger.info("---------------SQL>>>>>>>>>>>>>>>" + prepStmt.toString());
        ResultSet res = prepStmt.executeQuery();
        AuditTrail auditTrail = null;
        while (res.next()) {
            auditTrail = new AuditTrail();

            auditTrail.setActionPerformed(res.getString("action_performed"));;
            auditTrail.setId(res.getInt("id"));;
            auditTrail.setOriginatorId(res.getInt("originator_id"));
            auditTrail.setDateCreated(res.getString("date_created"));
            auditTrail.setDescription(res.getString("description"));
            auditTrail.setOriginatorIpAddress(res.getString("originator_ipaddress"));
            auditTrail.setOriginatorName(res.getString("originator_name"));
            auditTrail.setOriginatorType(res.getString("originator_type"));
            auditTrail.setOriginatorSchoolAdminId(res.getInt("originatorschooladmin_id"));
            auditTrails.add(auditTrail);
        }
        PortalDatabase.source.returnConnection(connection);
        return auditTrails;
    }

    public boolean createAuditTrail(AuditTrail auditTrail) throws SQLException {
        int portalAdminId = getLastID("admins") + 1;
        String sqlQuery = "insert into audittrail values (" + portalAdminId + ",'" + auditTrail.getOriginatorName() + "',now(),'" + auditTrail.getActionPerformed() + "','" + auditTrail.getDescription() + "','" + auditTrail.getOriginatorIpAddress() + "','" + auditTrail.getOriginatorId() + "','" + auditTrail.getOriginatorSchoolAdminId() + "','" + auditTrail.getOriginatorType() + "')";
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