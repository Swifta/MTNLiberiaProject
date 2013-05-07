/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.AdminRole;
import com.swifta.schoolportal.entities.PortalAdmin;
import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.utils.PortalDatabase;
import com.swifta.schoolportal.utils.SendUserPin;
import com.swifta.schoolportal.utils.UsernameGenerator;
import java.io.IOException;
import java.sql.PreparedStatement;
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

    public List<AdminRole> getAllAdminRoles() throws SQLException {
        String query = "select * from adminrole";

        logger.info("Query : " + query);
        ArrayList<AdminRole> adminRoles = new ArrayList<AdminRole>();
        JDCConnection con = PortalDatabase.source.getConnection();
        ResultSet res = con.createStatement().executeQuery(query);
        AdminRole adminRole = null;
        while (res.next()) {
            adminRole = new AdminRole();

            adminRole.setDescription(res.getString("description"));
            adminRole.setName(res.getString("name"));
            adminRole.setId(res.getInt("id"));
            adminRoles.add(adminRole);
        }
        //con.close();
        PortalDatabase.source.returnConnection(con);
        return adminRoles;
    }

    public List<PortalAdmin> getAllPortalAdmins() throws SQLException {
        String query = "select * from admins";

        logger.info("Query : " + query);
        ArrayList<PortalAdmin> portalAdmins = new ArrayList<PortalAdmin>();
        JDCConnection con = PortalDatabase.source.getConnection();
        ResultSet res = con.createStatement().executeQuery(query);
        PortalAdmin portalAdmin = null;
        while (res.next()) {
            logger.info("------------------------looping admins");

            portalAdmin = new PortalAdmin();

            portalAdmin.setDateCreated(res.getString("datecreated"));
            portalAdmin.setUsername(res.getString("username"));
            portalAdmin.setId(res.getInt("id"));
            portalAdmin.setRoleName(this.getAdminRoleName(portalAdmin.getUsername()));
            portalAdmin.setPassword(res.getString("password"));


            portalAdmins.add(portalAdmin);
        }
        //con.close();
        PortalDatabase.source.returnConnection(con);
        return portalAdmins;
    }

    public void deleteAdminRole(String portalAdminId) throws SQLException {
        String sqlQuery = "delete from admin_adminrole where admins_id = ?";
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, portalAdminId);
        prepStmt.execute();
        PortalDatabase.source.returnConnection(connection);
    }

    public void deletePortalAdmin(String portalAdminId) throws SQLException {
        String sqlQuery = "delete from admins where id = ?";
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, portalAdminId);
        prepStmt.execute();
        PortalDatabase.source.returnConnection(connection);
    }

    public List<School> getAllSchools() throws SQLException {
        String query = "select * from Partner_Service_Unit";

//    String query = "select * from Partner_Service_Unit where has_admin = 0";
        logger.info("Query : " + query);
        ArrayList<School> schools = new ArrayList<School>();
        JDCConnection con = PortalDatabase.source.getConnection();
        ResultSet res = con.createStatement().executeQuery(query);
        School school = null;
        while (res.next()) {
            school = new School();

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
//       send.sendMessage("Your username for the school portal is " + username, admin.getPhoneNo());

        return ex;
    }

    public boolean createPortalAdmin(PortalAdmin admin) throws SQLException, IOException, JSONException {
        int portalAdminId = getLastID("admins") + 1;
        String sqlQuery = "insert into admins values (" + portalAdminId + ",'" + admin.getUsername() + "','" + admin.getPassword() + "',now())";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        PortalDatabase.source.returnConnection(connection);
        createAdminRoles(admin.getRoleId(), portalAdminId);
        return ex;
    }

    public void createAdminRoles(String adminRoleId, int portalAdminId) throws SQLException {
        String sqlQuery = "insert into admin_adminrole(id,admins_id,adminrole_id) values (" + (getLastID("admin_adminrole") + 1) + ",'" + portalAdminId + "','" + adminRoleId + "')";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        connection.createStatement().execute(sqlQuery);
        PortalDatabase.source.returnConnection(connection);

    }

    public AdminRole getRoleByName(String roleName) throws SQLException {
        String sqlQuery = "select * from adminrole where name = ?";
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, roleName);
        ResultSet res = prepStmt.executeQuery();
        res.next();
        AdminRole adminRole = new AdminRole();
        while (res.next()) {
            adminRole.setDescription(res.getString("description"));
            adminRole.setName(res.getString("name"));
            adminRole.setId(res.getInt("id"));
        }
        logger.info("role Name : " + adminRole.getName());
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return adminRole;
    }

    public PortalAdmin getAdminById(String portalAdminId) throws SQLException {
        String sqlQuery = "select * from admins where id = ? order by id";
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, portalAdminId);

        ResultSet res = prepStmt.executeQuery();
        PortalAdmin portalAdmin = new PortalAdmin();
        while (res.next()) {
            portalAdmin.setUsername(res.getString("username"));
            portalAdmin.setPassword(res.getString("password"));
            portalAdmin.setId(res.getInt("id"));
            portalAdmin.setDateCreated(res.getString("datecreated"));
            portalAdmin.setRoleId(String.valueOf(getAdminRoleId(portalAdmin.getUsername())));
        }
        logger.info("user Name : " + portalAdmin.getUsername() + ">>>>>" + prepStmt.toString());
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return portalAdmin;
    }

    public boolean updatePortalAdmin(PortalAdmin portalAdmin) throws SQLException, IOException, JSONException {
        String sqlQuery = "update admins set username = ?,password = ? where id = " + portalAdmin.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, portalAdmin.getUsername().trim());
        prepStmt.setString(2, portalAdmin.getPassword().trim());
        boolean ex = prepStmt.execute();

        PortalDatabase.source.returnConnection(connection);
        deleteAdminRole(String.valueOf(portalAdmin.getId()));
        createAdminRoles(portalAdmin.getRoleId(), portalAdmin.getId());
        //send sms to user
        return ex;
    }

    public boolean updateAdmin(SchoolAdmin schoolAdmin) throws SQLException, IOException, JSONException {
        String sqlQuery = "update schooladmins  set emailaddress = ?, firstname = ?,lastname = ?,mobile =?,username = ? where id = " + schoolAdmin.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, schoolAdmin.getEmailAddress().trim());
        prepStmt.setString(2, schoolAdmin.getFirstName().trim());
        prepStmt.setString(3, schoolAdmin.getLastName().trim());
        prepStmt.setString(4, schoolAdmin.getPhoneNo().trim());
        prepStmt.setString(5, schoolAdmin.getUsername().trim());
        boolean ex = prepStmt.execute();
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

    public boolean existingPortalAdmin(PortalAdmin portalAdmin) throws SQLException {
        logger.info("existing portal admin ------------------");
        String sqlQuery = "select * from admins where username = ?";
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, portalAdmin.getUsername().trim());
        ResultSet res = prepStmt.executeQuery();
        boolean ex = false;

        PortalAdmin newPortalAdmin = new PortalAdmin();
        while (res.next()) {
            newPortalAdmin.setId(res.getInt("id"));
            if (!ex && newPortalAdmin.getId() != 0 && portalAdmin.getId() == 0) {
                ex = true;
            } else if (!ex && newPortalAdmin.getId() != 0 && portalAdmin.getId() != 0) {
                if (newPortalAdmin.getId() != portalAdmin.getId()) {
                    ex = true;
                }
            } else {
                ex = false;
            }
            if (ex) {
                break;
            }
        }
        logger.info("the last part of the iteration");
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
        String sqlQuery = "select * from Partner_Service_Unit where id = ?";
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, schoolID);
        ResultSet res = prepStmt.executeQuery();
        School sch = new School();
        while (res.next()) {
            sch.setName(res.getString("name"));
            sch.setSchoolCode(res.getString("school_code"));
            sch.setId(res.getInt("id"));
        }
        logger.info("School Name : " + sch.getName() + ">>>>" + sqlQuery);
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return sch;
    }

    public String getAdminRoleName(String username) throws SQLException {
        String sqlQuery = "select ar.name from adminrole ar, admins a,admin_adminrole aar where a.username = ? and aar.adminrole_id = ar.id and aar.admins_id = a.id";
        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, username);
        ResultSet res = prepStmt.executeQuery();
        String roleName = "N/A";
        while (res.next()) {
            Object retrievedRole = res.getString("name");
            if (retrievedRole != null) {
                roleName = (String) retrievedRole;
                logger.info(roleName + "----------------the role name");
            }
        }
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return roleName;
    }

    public int getAdminRoleId(String username) throws SQLException {
        String sqlQuery = "select ar.id from adminrole ar, admins a,admin_adminrole aar where a.username = ? and aar.adminrole_id = ar.id and aar.admins_id = a.id";
        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, username);
        ResultSet res = prepStmt.executeQuery();
        int roleName = 0;
        while (res.next()) {
            Object retrievedRole = res.getInt("id");
            if (retrievedRole != null) {
                roleName = (Integer) retrievedRole;
                logger.info(roleName + "----------------the role name");
            }
        }
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return roleName;
    }

    public SchoolAdmin getSchoolAdminDetails(int schoolAdminId) throws SQLException {
        String sqlQuery = "select * from schooladmins where id = ?";
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, schoolAdminId);
        ResultSet res = prepStmt.executeQuery();
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
            break;
        }
        logger.info("School Name : " + schAdmin.getEmailAddress());
        //connection.close();
        PortalDatabase.source.returnConnection(con);
        return schAdmin;
    }
}