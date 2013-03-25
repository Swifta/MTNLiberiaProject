/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.liberia.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import main.PropertyFileReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class DBUtils {

    private Logger logger = Logger.getLogger(DBUtils.class);
    public static PropertyFileReader fr = new PropertyFileReader("/opt/swifta/properties/schoolportal.properties");
    public static Connection connection;

    static {
        try {
            Properties p = fr.getAllProperties();
            Class.forName(p.getProperty("driver"));
            DBUtils.connection = DriverManager.getConnection(p.getProperty("url"), p.getProperty("username"), p.getProperty("password"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String createSchool(String name) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException {
        if (findSchoolID(name) <= 0) {
            String sqlQuery = "insert into partner_service_unit(partner_id,name,payment_mode_id,has_admin) values(1,'" + name.replace("'", "\\'") + "',1,0)";
            DBUtils.connection.createStatement().execute(sqlQuery);
            return sqlQuery;
        } else {
            return "";
        }
    }

    public int findSchoolID(String name) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException {
        ResultSet res = DBUtils.connection.createStatement().executeQuery("select id from partner_service_unit where name = '" + name.replace("'", "\\'") + "'");
        while (res.next()) {
            return res.getInt("id");
        }
        return 0;
    }

    public int findStudentID(String name, int schoolID) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException {
        ResultSet res = DBUtils.connection.createStatement().executeQuery("select id from person_info where name = '" + name.replace("'", "\\'") + "' and payment_serviceunit_id = " + schoolID);
        while (res.next()) {
            return res.getInt("id");
        }
        return 0;

    }

    public String createStudent(String ident, String name, int schoolID) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
        if (findStudentID(name, schoolID) <= 0) {
            String sqlQuery = "insert into person_info(name,identification_no,payment_serviceunit_id,last_update) values('" + name.replace("'", "\\'") + "','" + ident + "'," + schoolID + ",now())";
            DBUtils.connection.createStatement().execute(sqlQuery);
            return sqlQuery;
        }
        return null;
    }
}
