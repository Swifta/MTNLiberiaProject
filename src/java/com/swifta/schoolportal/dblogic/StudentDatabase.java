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
    private School sch = new School();
    //call existingStudents method before createStudent method

    public boolean createStudent(Student student) throws SQLException, IOException, JSONException {
        String sqlQuery = "insert into person_info values (" + (getLastID("person_info") + 1) + ",now(),'" + student.getName() + "','" + student.getIdentNo() + "'," + sch.getId() + ")";
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
        return existingStudent(student, "");
    }

    public boolean existingStudent(Student student, String schoolName) throws SQLException {
        if (student.getId() == 0) {
            sch = this.getSchoolDetails(schoolName);
            String regCode = sch.getSchoolCode();
            logger.info("The registration code of the school is >>>>>>" + regCode);
            regCode = regCode == null ? "" : regCode.toUpperCase();
            student.setIdentNo(regCode + student.getIdentNo());
        }
        String sqlQuery = "select * from person_info where lower(identification_no) = '" + student.getIdentNo().toLowerCase().trim() + "'";
        
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        boolean ex = false;
        Student newStudent = new Student();
        while (res.next()) {
            newStudent.setId(res.getInt("id"));
        }
        if (newStudent.getId() != 0 && student.getId() == 0) {
            ex = true;
        } else if (newStudent.getId() != 0 && student.getId() != 0) {
            if (newStudent.getId() != student.getId()) {
                ex = true;
            }
        } else {
            ex = false;
        }
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
            School sch = this.getSchoolDetails(res.getInt("payment_serviceunit_id"));
            student.setSchoolCode(sch.getSchoolCode());
            student.setSchoolDetails(sch.getName().toUpperCase().trim()+" - "+sch.getSchoolCode().toUpperCase().trim());
        }
        logger.info("student Name : " + student.getName());
        //connection.close();
        PortalDatabase.source.returnConnection(connection);
        return student;
        
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
    
    public School getSchoolDetails(String schoolName) throws SQLException {
        String sqlQuery = "select * from Partner_Service_Unit where lower(name) = '" + schoolName.toLowerCase() + "'";
        JDCConnection connection = PortalDatabase.source.getConnection();
        ResultSet res = connection.createStatement().executeQuery(sqlQuery);
        School sch = new School();
        while (res.next()) {
            sch.setName(res.getString("name"));
            sch.setId(res.getInt("id"));
            sch.setSchoolCode(res.getString("school_code"));
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
