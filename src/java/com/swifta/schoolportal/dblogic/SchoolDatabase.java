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
        String sqlQuery = "select * from schooladmins where username = '" + username + "'";
        logger.info(sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);

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
        PortalDatabase.source.returnConnection(connection);
        return admin;
    }

    public List<Student> getAllSchoolStudents(int schoolId) throws SQLException {
        String sqlQuery = "select * from person_info where payment_serviceunit_id = " + schoolId;
        logger.info(sqlQuery);

        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);

        ArrayList<Student> students = new ArrayList<Student>();

        while (res.next()) {
            Student st = new Student();

            st.setName(res.getString("name"));
            st.setIdentNo(res.getString("identification_no"));
            st.setLastUpdate(res.getString("last_update"));
            st.setId(res.getInt("id"));

            students.add(st);
        }
        PortalDatabase.source.returnConnection(connection);
        return students;
    }

    public List<TransactionHistory> getHistory() throws SQLException {
        portalSession = new PortalSession();

        int schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_admin_school_id")));

        String sqlQuery = "SELECT th.last_update as 'Date', ps.name as 'Name of Student',th.amount as 'Amount Paid',"
                + "tr.payer_id as 'Paid by',tr.payment_ref as 'Payment Reference', tr.fundamo_id as 'Transaction ID'"
                + "from Transactions tr, Transaction_History th, Person_info ps,Partner_Service_Unit pu where tr.id ="
                + " th.transaction_id and ps.identification_no = tr.person_id and ps.payment_serviceunit_id = pu.id"
                + " and tr.status_code_id = '01' and pu.id = " + schoolId;
        logger.info(sqlQuery);

        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);

        ArrayList<TransactionHistory> histories = new ArrayList<TransactionHistory>();

        while (res.next()) {

            TransactionHistory th = new TransactionHistory();
            th.setDate(res.getString(1));
            th.setStudentName(res.getString(2));
            th.setAmountPaid(res.getString(3));
            th.setPaidBy(res.getString(4));
            th.setPaymentRef(res.getString(5));
            th.setTransactionID(res.getString(6));

            histories.add(th);

        }
        PortalDatabase.source.returnConnection(connection);
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
        String sqlQuery = "insert into partner_service_unit values (" + (getLastID("partner_service_unit") + 1) + ",'" + school.getPartnerServiceId() + "','" + school.getName() + "','" + school.getPaymentModeId() + "','1')";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        //connection.close();

        PortalDatabase.source.returnConnection(connection);

        //send sms to user

        return ex;
    }

    public boolean updateSchool(School school) throws SQLException, IOException, JSONException {
        String sqlQuery = "update partner_service_unit set name = '" + school.getName() + "' where id = " + school.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);

        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        return ex;
    }

    public boolean existingSchool(String name) throws SQLException {
        String sqlQuery = "select * from partner_service_unit where lower(name) = '" + name + "'";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        boolean ex = res.next();

        PortalDatabase.source.returnConnection(connection);

        //connection.close();
        return ex;
    }
}
