/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.afrinnova.api.schoolfees.service.model;

import com.afrinnova.api.schoolfees.jdbcprop.JDCConnectionDriver;
import com.afrinnova.api.schoolfees.service.exception.AccountDAOException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author mAladeojebi
 */
public class AccountLookup {

    public static String sDriverName = null;
    public static String sServerName = null;
    public static String sPort = null;
    public static String sDatabaseName = null;
    public static String sUserName = null;
    public static String sPassword = null;
    public static String sURL = null;
    public static String propsFilePath = null;
    private static final Logger logger = Logger.getLogger("com.afrinnova.api.schoolfees.service.model.AccountLookup");

    public static java.sql.Date getCurrentJavaSqlDate() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Date(today.getTime());
    }
//    public AccountLookup() throws IOException {
//
//        
//    }
    
    static Properties properties = new Properties();

    static {
        
        try {
          
            String os = System.getProperty("os.name").toLowerCase();

            logger.info("Operating System " + os);

            if (os.startsWith("sun")) {

                propsFilePath = "/opt/swifta/server/properties/database.properties";
            } else {
                propsFilePath = "/Users/modupealadeojebi/Downloads/database.properties";
            }
            
            logger.info("**********Loading database properties");

            loadProperties(propsFilePath);

            logger.info("**********properties loading successfully");
            
            sURL = "jdbc:mysql://" + sServerName + ":" + sPort + "/" + sDatabaseName;
            new JDCConnectionDriver("org.gjt.mm.mysql.Driver", sURL, sUserName, sPassword);
        } catch (Exception e) {
        }
    }

    public Connection getConnection() throws SQLException {
//        return DriverManager.getConnection("jdbc:jdc:jdcpool");
        return DriverManager.getConnection(sURL, sUserName, sPassword);
    }
    

//    private static Connection getDBConnection() throws AccountDAOException, IOException {
//
//
//        try {
//            Connection conn = null;
//
//            String os = System.getProperty("os.name").toLowerCase();
//
//            logger.info("Operating System " + os);
//
//            if (os.startsWith("sun")) {
//
//                propsFilePath = "/opt/swifta/server/properties/database.properties";
//            } else {
//                propsFilePath = "/Users/modupealadeojebi/Downloads/database.properties";
//            }
//
//            logger.info("Reading from properties file " + propsFilePath);
//
////            sDriverName = "com.mysql.jdbc.Driver";
////            sServerName = "localhost";
////            sPort = "3306";
////            sDatabaseName = "AfrinnovaDB";
////            sUserName = "root";
////            sPassword = "swifta";
////
////            //Class.forName("sDriverName").newInstance();
//
//            logger.info("**********Loading database properties");
//
//            loadProperties(propsFilePath);
//
//            logger.info("**********properties loading successfully");
//
//            sURL = "jdbc:mysql://" + sServerName + ":" + sPort + "/" + sDatabaseName;
//
//            logger.info("*********using connection String: " + sURL);
//
//            conn = DriverManager.getConnection(sURL, sUserName, sPassword);
//
//            System.out.println("Connection established to database");
//
//            return conn;
//        } catch (SQLException e) {
//            throw new AccountDAOException("Exception caught while making connection: " + e.getMessage());
//        }
//    }

    public  Account01 getAccountref(String acctref) throws AccountDAOException, IOException {

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Account01 ret = null;

        try {
            c = getConnection();

            logger.info("Fetching balance for " + acctref);
            ps = c.prepareStatement("select Partner_balance.maximum_amount, Partner_balance.minimum_amount "
                    + "from Person_info ,Partner_Service_Unit,Partner_balance "
                    + "where Person_info.payment_serviceunit_id = Partner_Service_Unit.id "
                    + "and Partner_Service_Unit.payment_mode_id = Partner_balance.paymode_id "
                    + "and Person_info.identification_no= ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ps.setString(1, acctref);
            rs = ps.executeQuery();
            if (rs.first()) {
                ret = new Account01(rs.getLong(1), rs.getLong(2));
            }
            rs.close();
            ps.close();

            c.close();

            return ret;
        } catch (SQLException se) {
            logger.info("School Id is invalid, not in the DB");
            throw new AccountDAOException("SQLException:" + se.getMessage());
        }
    }

    public void insertInstallmentPay(String payerAccountIdentifier, String customerName, String accountRef, long amount, String paymentRef, String fundamoTransactionID) throws AccountDAOException, IOException {

        int serno;
        serno = 1;

        String ref = null;


        java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());



        Connection c = null;
        PreparedStatement ps = null;
        Statement st = null;
        ResultSet rs = null;

        try {

            c = getConnection();


            st = c.createStatement();
            rs = st.executeQuery("select max(id) from Transaction_History");

            while (rs.next()) {
                serno = rs.getInt(1) + 1;
            }
            rs.close();
            st.close();
            c.close();

            c = getConnection();


            ps = c.prepareStatement("select id from Transactions where person_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ps.setString(1, accountRef);
            rs = ps.executeQuery();
            if (rs.first()) {
                ref = rs.getString(1);
            }
            rs.close();
            ps.close();

            c.close();

            c = getConnection();

            logger.info("inserting record to transaction_history table");
            ps = c.prepareStatement("insert into Transaction_History values(?,?,?,?,?,?,?,?)");
            ps.setInt(1, serno);
            ps.setTimestamp(2, sqlDate);
            ps.setString(3, customerName);
            ps.setString(4, payerAccountIdentifier);
            ps.setString(5, fundamoTransactionID);
            ps.setString(6, paymentRef);
            ps.setDouble(7, amount);
            ps.setString(8, ref);


            int transactions = ps.executeUpdate();

            System.out.println(transactions + "rows affected");

            rs.close();
            ps.close();

            c.close();



        } catch (SQLException se) {
            logger.info("SQLException caught while inserting record to transactions history table" + se.getMessage());
            throw new AccountDAOException("SQLException:" + se.getMessage());
        }

    }

    public TransactionOb getPayment(String acctref) throws AccountDAOException, IOException {

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        TransactionOb ret = null;

        try {
            c = getConnection();

            logger.info("::::Verifying payment status::::");
            ps = c.prepareStatement("select person_id, status_code_id, amount from Transactions where person_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setString(1, acctref);
            rs = ps.executeQuery();
            if (rs.first()) {
                ret = new TransactionOb(rs.getString(1), rs.getString(2), rs.getLong(3));
            }
            rs.close();
            ps.close();

            c.close();

            return ret;
        } catch (SQLException se) {
            logger.info("School Id is invalid, not in the DB");
            throw new AccountDAOException("SQLException:" + se.getMessage());
        }
    }

    public void insertTransaction(String payerAccountIdentifier, String customerName, String accountRef, long amount, String paymentRef, String fundamoTransactionID, String thirdPartyTransactionID, String statusCode) throws AccountDAOException, IOException {

        int serno;
        serno = 1;


        java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());



        Connection c = null;
        PreparedStatement ps = null;
        Statement st = null;
        ResultSet rs = null;

        try {

            c = getConnection();


            st = c.createStatement();
            rs = st.executeQuery("select max(id) from Transactions");

            while (rs.next()) {
                serno = rs.getInt(1) + 1;
            }
            rs.close();
            st.close();
            c.close();



            c = getConnection();

            logger.info("inserting record into transactions table");
            ps = c.prepareStatement("insert into Transactions values(?,?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, serno);
            ps.setTimestamp(2, sqlDate);
            ps.setString(3, accountRef);
            ps.setDouble(4, amount);
            ps.setString(5, payerAccountIdentifier);
            ps.setString(6, customerName);
            ps.setString(7, paymentRef);
            ps.setString(8, fundamoTransactionID);
            ps.setString(9, statusCode);
            ps.setNull(10, java.sql.Types.VARCHAR);

            int transactions = ps.executeUpdate();

            System.out.println(transactions + "rows affected");

            rs.close();
            ps.close();

            c.close();



        } catch (SQLException se) {
            logger.info("SQLException caught while inserting record to transactions table" + se.getMessage());
            throw new AccountDAOException("SQLException:" + se.getMessage());
        }

    }

    private static void loadProperties(String propsFilePath) throws IOException {

        System.out.println("Reading configuration file " + propsFilePath + "...");
        FileInputStream propsFile = new FileInputStream(propsFilePath);
        properties.load(propsFile);
        propsFile.close();

        sDriverName = properties.getProperty("driver-name");
        sServerName = properties.getProperty("ip-address");
        sPort = properties.getProperty("port");
        sDatabaseName = properties.getProperty("db-name");
        sUserName = properties.getProperty("username");
        sPassword = properties.getProperty("password");



    }

    public void updateTransaction(String payerAccountIdentifier, String accountRef, long amount, String customerName, String paymentRef, String fundamoTransactionID) throws AccountDAOException, IOException {
        int serno;
        serno = 1;


        java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());



        Connection c = null;
        PreparedStatement ps = null;
        Statement st = null;
        ResultSet rs = null;

        try {

            c = getConnection();


            st = c.createStatement();
            rs = st.executeQuery("select max(id) from Transactions");

            while (rs.next()) {
                serno = rs.getInt(1) + 1;
            }
            rs.close();
            st.close();
            c.close();



            c = getConnection();

            logger.info("updating records in transaction table for student: " + accountRef);
            String sql = "update Transactions set last_update = ?, payer_id = ? , amount = ? , payer_name = ? , fundamo_id = ? where person_id = ?";
            ps = c.prepareStatement(sql);
            //ps = c.prepareStatement("update Transactions set values(?,?,?,?,?,?,?,?,?)");
            ps.setTimestamp(1, sqlDate);
            ps.setString(2, payerAccountIdentifier);
            ps.setDouble(3, amount);
            ps.setString(4, customerName);
            ps.setString(5, fundamoTransactionID);
            ps.setString(6, accountRef);

            int transactions = ps.executeUpdate();

            System.out.println(transactions + "rows affected");

            logger.info("Record updated successfully for " + accountRef);

            rs.close();
            ps.close();

            c.close();



        } catch (SQLException se) {
            logger.info("SQLException caught while updating record to transactions table" + se.getMessage());
            throw new AccountDAOException("SQLException:" + se.getMessage());
        }
    }
}
