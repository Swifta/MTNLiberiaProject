/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.PaymentMode;
import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.utils.PortalDatabase;
import com.swifta.schoolportal.utils.SendUserPin;
import com.swifta.schoolportal.utils.UsernameGenerator;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.me.JSONException;

/**
 *
 * @author Opeyemi
 */
public class PortalAdminDatabase {

    private Logger logger = Logger.getLogger(PortalAdminDatabase.class);

    public List<School> getAllSchools() throws SQLException {
        String query = "select * from Partner_Service_Unit";

//    String query = "select * from Partner_Service_Unit where has_admin = 0";
        logger.info("Query : " + query);
        ArrayList<School> schools = new ArrayList<School>();
        JDCConnection con = PortalDatabase.source.getConnection();
        ResultSet res = con.createStatement().executeQuery(query);
        while (res.next()) {
            School school = new School();

            school.setName(res.getString("name"));
            school.setId(res.getInt("id"));
            school.setPaymentModeType(res.getObject("payment_mode_id").toString());
            school.setPartnerServiceName(res.getObject("partner_id").toString());
            school.setSchoolCode(res.getString("school_code"));

            schools.add(school);
        }
        //con.close();
        PortalDatabase.source.returnConnection(con);
        return schools;
    }

    public boolean createSchoolAdmin(SchoolAdmin admin) throws SQLException, IOException, JSONException {
        School sch = this.getSchoolDetails(admin.getSchoolID());
        String username = new UsernameGenerator().generateUsername(sch.getName());
        String sqlQuery = "insert into schooladmins values (" + (getLastID("schooladmins") + 1) + ",'" + admin.getFirstName() + "','" + admin.getLastName() + "','" + admin.getPhoneNo() + "','" + admin.getEmailAddress() + "'," + admin.getSchoolID() + ",'" + username + "',now())";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        if (!ex) {
            sqlQuery = "update Partner_Service_Unit set has_admin = 1 where id = " + admin.getSchoolID();
            connection.createStatement().execute(sqlQuery);
        }
        //connection.close();

        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        logger.info("Sending username to " + admin.getFirstName() + ", " + admin.getLastName());
        SendUserPin send = new SendUserPin();
        send.sendMessage("Your username for the school portal is " + username, admin.getPhoneNo());

        return ex;
    }

    public boolean updateAdmin(SchoolAdmin schoolAdmin) throws SQLException, IOException, JSONException {
        String sqlQuery = "update schooladmins  set emailaddress = '" + schoolAdmin.getEmailAddress() + "', firstname ='" + schoolAdmin.getFirstName() + "', "
                + "lastname = '" + schoolAdmin.getLastName() + "', mobile ='" + schoolAdmin.getPhoneNo() + "',"
                + "username = '" + schoolAdmin.getUsername() + "' where id = " + schoolAdmin.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        return ex;
    }

    public boolean existingSchoolAdmin(SchoolAdmin admin) throws SQLException {
        String sqlQuery = "select * from schooladmins where mobile = '" + admin.getPhoneNo() + "'";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        boolean ex = res.next();

        PortalDatabase.source.returnConnection(connection);

        //connection.close();
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

    public List<SchoolAdmin> getAllAdmins() throws SQLException {

        ArrayList<SchoolAdmin> admins = new ArrayList<SchoolAdmin>();

        String sqlQuery = "select * from schooladmins";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        while (res.next()) {
            SchoolAdmin ad = new SchoolAdmin();

            ad.setEmailAddress(res.getString("emailaddress"));
            ad.setFirstName(res.getString("firstname"));
            ad.setLastName(res.getString("lastname"));
            ad.setPhoneNo(res.getString("mobile"));
            ad.setUsername(res.getString("username"));
            School sch = this.getSchoolDetails(res.getInt("schoolid"));
            ad.setSchoolName(sch.getName());
            ad.setSchoolID(sch.getId());
            ad.setId(res.getInt("id"));

            admins.add(ad);
        }
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return admins;
    }

    public School getSchoolDetails(int schoolID) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where id = " + schoolID;
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        School sch = new School();
        while (res.next()) {
            sch.setName(res.getString("name"));
            sch.setSchoolCode(res.getString("school_code"));
            sch.setId(res.getInt("id"));
        }
        logger.info("School Name : " + sch.getName());
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return sch;
    }

    public SchoolAdmin getSchoolAdminDetails(int schoolAdminId) throws SQLException {
        String sqlQuery = "select * from schooladmins where id = " + schoolAdminId;
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        SchoolAdmin schAdmin = new SchoolAdmin();
        while (res.next()) {
            schAdmin.setEmailAddress(res.getString("emailaddress"));
            schAdmin.setFirstName(res.getString("firstname"));
            schAdmin.setLastName(res.getString("lastname"));
            schAdmin.setPhoneNo(res.getString("mobile"));
            schAdmin.setUsername(res.getString("username"));
            schAdmin.setId(res.getInt("id"));
            School sch = this.getSchoolDetails(res.getInt("schoolid"));
            schAdmin.setSchoolName(sch.getName());
            schAdmin.setSchoolID(sch.getId());
            schAdmin.setId(res.getInt("id"));
        }
        logger.info("School Name : " + schAdmin.getEmailAddress());
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return schAdmin;
    }
}
