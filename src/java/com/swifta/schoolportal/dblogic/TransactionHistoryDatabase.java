/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.dblogic;

import com.swifta.schoolportal.entities.TransactionHistory;
import com.swifta.schoolportal.managedbeans.PortalSession;
import com.swifta.schoolportal.utils.AppValues;
import com.swifta.schoolportal.utils.DateUtilities;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author princeyekaso
 */
public class TransactionHistoryDatabase {

    private PortalSession portalSession;
    private Logger logger = Logger.getLogger(TransactionHistoryDatabase.class);
    public Double subTotal = 0.0;
    private DateUtilities dateUtility = new DateUtilities();
    private AppValues appValues = new AppValues();

    public boolean updateTransactionHistory(int transactionId) throws SQLException {
        String sqlQuery = "update Transaction_History set redeemed = true where id = ?";
        //    logger.info("history query.......>>>" + sqlQuery);

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, transactionId);
        boolean result = prepStmt.execute();

        PortalDatabase.source.returnConnection(con);
        return result;
    }

    public TransactionHistory getTransactionHistoryById(int transactionId) throws SQLException {
        TransactionHistory th = null;
        String sqlQuery = "SELECT * from Transaction_History where id = ?";
        //  logger.info("history query.......>>>" + sqlQuery);

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setInt(1, transactionId);
        ResultSet res = prepStmt.executeQuery();

        while (res.next()) {
            th = new TransactionHistory();
            th.setDate(res.getString("date_created"));
            th.setAmountPaid(String.valueOf(res.getDouble("amount")));
            th.setPaidBy(res.getString("payer_name"));
            th.setPaymentRef(res.getString("payment_ref"));
            th.setTransactionID(res.getString("transaction_id"));
            th.setRedeemed(res.getBoolean("redeemed"));
            th.setId(res.getInt("id"));
        }
        PortalDatabase.source.returnConnection(con);
        return th;
    }

    public List<TransactionHistory> getHistory(int monthId, String dateFrom, String dateTo) throws SQLException {
        portalSession = new PortalSession();
        TransactionHistory th = null;
        dateTo = dateUtility.incrementByADay(dateTo, appValues.DEFAULT_DATE_FORMAT);
        int schoolId = 0;
        if (portalSession.getAppSession().getAttribute("portal_admin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_admin_school_id")));
        }
        if (schoolId == 0 && portalSession.getAppSession().getAttribute("portal_schooladmin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_schooladmin_school_id")));
        }
        if (schoolId == 0 && portalSession.getAppSession().getAttribute("portal_schooladmin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_schooladmin_school_id")));
        }
        String sqlQuery = "SELECT th.date_created as 'Date', ps.name as 'Name of Student',th.amount as 'Amount Paid',"
                + "tr.payer_id as 'Paid by',tr.payment_ref as 'Payment Reference', tr.fundamo_id as 'Transaction ID',"
                + "th.redeemed as 'Redeemed',th.id as 'Id' from Transactions tr, Transaction_History th, Person_info ps,Partner_Service_Unit pu where tr.id ="
                + " th.transaction_id and ps.identification_no = tr.person_id and ps.payment_serviceunit_id = pu.id"
                + " and tr.status_code_id = '01' and th.date_created <= ? and th.date_created >= ? and pu.id = ? and Month(th.date_created) = ?";

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, dateTo);
        prepStmt.setString(2, dateFrom);
        prepStmt.setInt(3, schoolId);
        prepStmt.setInt(4, monthId);
        ResultSet res = prepStmt.executeQuery();
        ArrayList<TransactionHistory> histories = new ArrayList<TransactionHistory>();
        logger.info("---------------SQL>>>>>>>>>>>>>>>" + prepStmt.toString());
        Double amount = 0.0;
        while (res.next()) {

            th = new TransactionHistory();
            th.setDate(res.getString(1));
            th.setStudentName(res.getString(2));
            //  th.setAmountPaid(String.valueOf(res.getDouble(3) / 100));
            amount = res.getDouble(3);
            th.setAmountPaid(String.valueOf(amount));
            th.setPaidBy(res.getString(4));
            th.setPaymentRef(res.getString(5));
            th.setTransactionID(res.getString(6));
            th.setRedeemed(res.getBoolean(7));
            th.setId(res.getInt(8));

            histories.add(th);
            this.subTotal += amount;

        }
        PortalDatabase.source.returnConnection(con);
        return histories;

    }

    public List<TransactionHistory> getHistory(String dateFrom, String dateTo) throws SQLException {
        portalSession = new PortalSession();
        TransactionHistory th = null;
        dateTo = dateUtility.incrementByADay(dateTo, appValues.DEFAULT_DATE_FORMAT if (schoolId == 0 && portalSession.getAppSession().getAttribute("portal_schooladmin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_schooladmin_school_id")));
        }
        if (schoolId == 0 && portalSession.getAppSession().getAttribute("portal_schooladmin_school_id") != null) {
            schoolId = Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_schooladmin_school_id")));
        }
        String sqlQuery = "SELECT th.date_created as 'Date', ps.name as 'Name of Student',th.amount as 'Amount Paid',"
                + "tr.payer_id as 'Paid by',tr.payment_ref as 'Payment Reference', tr.fundamo_id as 'Transaction ID',"
                + "th.redeemed as 'Redeemed',th.id as 'Id' from Transactions tr, Transaction_History th, Person_info ps,Partner_S";

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.seted',th.id as 'Id' from Transactions tr, Transaction_History th, Person_info ps,Partner_Service_Unit pu where tr.id ="
                + " th.transaction_id and ps.identification_no = tr.person_id and ps.payment_serviceunit_id = pu.id"
                + " and tr.status_code_id = '01' and th.date_created <= ? and th.date_created >= ? and pu.id = ?";

        JDCConnection con = PortalDatabase.source.getConnection();
        PreparedStatement prepStmt = con.prepareStatement(sqlQuery);
        prepStmt.setString(1, dateTo);
        prepStmt.setString(2, dateFrom);
        prepStmt.setInt(3, schoolId);
        ResultSet res = prepStmt.executeQuery();
        ArrayList<TransactionHistory> histories = new ArrayList<TransactionHistory>();
        logger.info("---------------SQL>>>>>>>>>>>>>>>" + prepStmt.toString());
        Double amount = 0.0;
        while (res.next()) {

            th = new TransactionHistory();
            th.setDate(res.getString(1));
            th.setStudentName(res.getString(2));
            //  th.setAmountPaid(String.valueOf(res.getDouble(3) / 