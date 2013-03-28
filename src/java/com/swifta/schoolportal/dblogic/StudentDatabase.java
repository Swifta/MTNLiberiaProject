/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.Student;
import com.swifta.schoolportal.utils.PortalDatabase;
import com.swifta.schoolportal.utils.SendUserPin;
import com.swifta.schoolportal.utils.UsernameGenerator;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.json.me.JSONException;

/**
 *
 * @author princeyekaso
 */
public class StudentDatabase {

    private Logger logger = Logger.getLogger(StudentDatabase.class);

    public boolean createStudent(Student student, String schoolName) throws SQLException, IOException, JSONException {
        School sch = this.getSchoolDetails(schoolName);
        String sqlQuery = "insert into person_info values (" + (getLastID("person_info") + 1) + ",curdate(),'" + student.getName() + "','" + student.getIdentNo() + "'," + sch.getId() + ")";
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);
        if (!ex) {
            sqlQuery = "update person_info set name = '" + student.getName() + "' where lower(identification_no) = '" + student.getIdentNo().toLowerCase() + "'";
            connection.createStatement().execute(sqlQuery);
        }
        //connection.close();

        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        return ex;
    }

    public boolean updateStudent(Student student) throws SQLException, IOException, JSONException {
        String sqlQuery = "update person_info set name = '" + student.getName() + "', identification_no ='" + student.getIdentNo() + "' where id = " + student.getId();
        logger.info("Query : " + sqlQuery);
        JDCConnection connection = PortalDatabase.source.getConnection();
        boolean ex = connection.createStatement().execute(sqlQuery);

        PortalDatabase.source.returnConnection(connection);

        //send sms to user
        return ex;
    }

    public boolean existingStudent(Student student) throws SQLException {
        String sqlQuery = "select * from person_info where lower(identification_no) = '" + student.getIdentNo().toLowerCase() + "'";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        boolean ex = res.next();

        PortalDatabase.source.returnConnection(connection);

        //connection.close();
        return ex;
    }

    public Student getStudent(Long studentId) throws SQLException {
        String sqlQuery = "select * from person_info where id = " + studentId;
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        Student student = new Student();
        while (res.next()) {
            student.setName(res.getString("name"));
            student.setIdentNo(res.getString("identification_no"));
            student.setLastUpdate(res.getString("last_update"));
            student.setId(res.getInt("id"));
        }
        logger.info("student Name : " + student.getName());
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return student;

    }

    public School getSchoolDetails(String schoolName) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where lower(name) = '" + schoolName.toLowerCase() + "'";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        School sch = new School();
        while (res.next()) {
            sch.setName(res.getString("name"));
            sch.setId(res.getInt("id"));
        }
        logger.info("School Name : " + sch.getName());
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return sch;
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
