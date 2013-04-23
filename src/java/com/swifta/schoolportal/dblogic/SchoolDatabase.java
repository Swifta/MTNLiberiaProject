/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.entities.Student;
import com.swifta.schoolportal.entities.TransactionHistory;
import com.swifta.schoolportal.managedbeans.PortalSession;
import com.swifta.schoolportal.utils.PortalDatabase;
import com.swifta.schoolportal.utils.SendUserPin;
import com.swifta.schoolportal.utils.UsernameGenerator;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.me.JSONException;

/**
 *
 * @author Opeyemi
 */
public class SchoolDatabase {

    Logger logger = Logger.getLogger(SchoolDatabase.class);
    private PortalSession portalSession;

    public SchoolAdmin getSchoolAdminDetails(String username) throws SQLException {
        String sqlQuery = "select * from schooladmins where username = ?";
        logger.info(sqlQuery);

        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, username);
        ResultSet res = prepStmt.executeQuery();
        SchoolAdmin admin = null;

        while (res.next()) {

            admin = new SchoolAdmin();

            admin.setEmailAddress(res.getString("emailaddress"));
            admin.setFirstName(res.getString("firstname"));
            admin.setLastName(res.getString("lastname"));
            admin.setPhoneNo(res.getString("mobile"));
            admin.setUsername(res.getString("username"));

            School school = new PortalAdminDatabase().getSchoolDetails(res.getInt("schoolid"));
            admin.setSchoolName(school.getName());
            admin.setSchoolID(school.getId());

        }
        PortalDatabase.source.returnConnection(con);
        return admin;
    }

    public List<Student> getAllSchoolStudents(int schoolId) throws SQLException {
        String sqlQuery = "select * from Person_info where payment_serviceunit_id = ?";
        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, schoolId);
        ResultSet res = prepStmt.executeQuery();
        ArrayList<Student> students = new ArrayList<Student>();

        while (res.next()) {
            Student st = new Student();

            st.setName(res.getString("name"));
            st.setIdentNo(res.getString("identification_no"));
            st.setLastUpdate(res.getString("last_update"));
            st.setId(res.getInt("id"));

            students.add(st);
        }
        PortalDatabase.source.returnConnection(con);
        return students;
    }

    public List<TransactionHistory> getHistory() throws SQLException {
        portalSession = new PortalSession();
        TransactionHistory th = null;
        int schoolId = 0;
        if (portalSession.getAppSession().getAttribute("portal_admin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_admin_school_id")));
        }
        logger.info("-----------------------new school id " + schoolId);
        String sqlQuery = "SELECT th.last_update as 'Date', ps.name as 'Name of Student',th.amount as 'Amount Paid',"
                + "tr.payer_id as 'Paid by',tr.payment_ref as 'Payment Reference', tr.fundamo_id as 'Transaction ID'"
                + "from Transactions tr, Transaction_History th, Person_info ps,Partner_Service_Unit pu where tr.id ="
                + " th.transaction_id and ps.identification_no = tr.person_id and ps.payment_serviceunit_id = pu.id"
                + " and tr.status_code_id = '01' and pu.id = ?";
        logger.info("history query.......>>>" + sqlQuery);

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, schoolId);
        ResultSet res = prepStmt.executeQuery();
        ArrayList<TransactionHistory> histories = new ArrayList<TransactionHistory>();

        while (res.next()) {

            th = new TransactionHistory();
            th.setDate(res.getString(1));
            th.setStudentName(res.getString(2));
            //  th.setAmountPaid(String.valueOf(res.getDouble(3) / 100));
            th.setAmountPaid(String.valueOf(res.getDouble(3)));
            th.setPaidBy(res.getString(4));
            th.setPaymentRef(res.getString(5));
            th.setTransactionID(res.getString(6));

            histories.add(th);

        }
        PortalDatabase.source.returnConnection(con);
        return histories;

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

    public boolean createSchool(School school) throws SQLException, IOException, JSONException {
        String sqlQuery = "insert into Partner_Service_Unit (id,partner_id,name,payment_mode_id,has_admin,school_code) values (?,?,?,?,?,?)";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setInt(1, (getLastID("Partner_Service_Unit") + 1));
        prepStmt.setString(2, school.getPartnerServiceId());
        prepStmt.setString(3, school.getName());
        prepStmt.setString(4, school.getPaymentModeId());
        prepStmt.setString(5, "1");
        prepStmt.setString(6, school.getSchoolCode().toUpperCase().trim());
        boolean ex = prepStmt.execute();

        //connection.close();

        PortalDatabase.source.returnConnection(connection);

        //send sms to user

        return ex;
    }

    public boolean updateSchool(School school) throws SQLException, IOException, JSONException {
        String sqlQuery = "update Partner_Service_Unit set name = ?,school_code= ? where id = " + school.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setString(1, school.getName());
        prepStmt.setString(2, school.getSchoolCode().toUpperCase().trim());
        boolean ex = prepStmt.execute();

        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        return ex;
    }

    public boolean existingSchool(School school) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where lower(name) = ? or lower(school_code) = ?";

        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, school.getName().toLowerCase().trim());
        prepStmt.setString(2, school.getSchoolCode().toLowerCase().trim());
        ResultSet res = prepStmt.executeQuery();
        boolean ex = false;
        School newSchool = new School();
        while (res.next()) {
            newSchool.setId(res.getInt("id"));
            if (!ex && newSchool.getId() != 0 && school.getId() == 0) {
                ex = true;
            } else if (!ex && newSchool.getId() != 0 && school.getId() != 0) {
                if (newSchool.getId() != school.getId()) {
                    ex = true;
                }
            } else {
                ex = false;
            }
            if (ex) {
                break;
            }
            logger.info(ex + ">>>The school id retrieved is...>>>>>>>>>>>><<<<<<<<<<<<<<" + newSchool.getId() + "<<<<<<<<<<<<<<>>>>>>>>>>>>>>" + school.getId());

        }
        logger.info("the last part of the iteration");
        PortalDatabase.source.returnConnection(con);

        //connection.close();
        return ex;
    }

    public boolean existingSchoolName(School school) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where lower(name) = ?";

        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, school.getName().toLowerCase().trim());
        ResultSet res = prepStmt.executeQuery();
        boolean ex = res.next();

        PortalDatabase.source.returnConnection(con);

        //connection.close();
        return ex;
    }

    public boolean existingSchoolCode(School school) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where lower(school_code) = ?";

        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, school.getSchoolCode().toLowerCase().trim());
        ResultSet res = prepStmt.executeQuery();
        boolean ex = res.next();

        PortalDatabase.source.returnConnection(con);

        //connection.close();
        return ex;
    }
}
