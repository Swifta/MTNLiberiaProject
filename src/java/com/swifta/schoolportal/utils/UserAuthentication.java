/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

import com.swifta.schoolportal.dblogic.JDCConnection;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
public class UserAuthentication {

    private Logger logger = Logger.getLogger(UserAuthentication.class);

    public UserAuthentication() {
    }

    public boolean authenticateAdmin(String username, String password) throws IOException, SQLException {
        String sqlQuery = "select * from admins where username = ? and password= ?";

        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, username);
        prepStmt.setString(2, password);
        ResultSet res = prepStmt.executeQuery();
        PortalDatabase.source.returnConnection(con);
        return res.next();
    }

    public String getFilePath() {
        logger.info("Getting properties file path ... ");
        String fileName = "";
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            fileName = "C:\\PropertyFiles\\database.properties";
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0) {
            fileName = "/opt/swifta/server/properties/database.properties";
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0) {
            fileName = "/opt/swifta/server/properties/database.properties";
        }
        if (fileName.length() <= 0) {
            fileName = "/opt/swifta/server/properties/database.properties";
        }
        logger.info("Fetching properties file from : " + fileName);
        return fileName;
    }
}
