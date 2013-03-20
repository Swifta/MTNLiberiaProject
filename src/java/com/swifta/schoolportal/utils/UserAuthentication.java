/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

import com.swifta.schoolportal.dblogic.JDCConnection;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import main.PropertyFileReader;
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
        String sqlQuery = "select * from admins where username = '" + username + "' and password= '" + password + "'";
        logger.info(sqlQuery);
        JDCConnection con = PortalDatabase.source.getConnection();
        ResultSet res = con.createStatement().executeQuery(sqlQuery);
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
        logger.info("Fetching properties file from : " + fileName);
        return fileName;
    }
}
