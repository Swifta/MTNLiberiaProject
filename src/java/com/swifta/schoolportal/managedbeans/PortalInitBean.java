/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.dblogic.JDCConnectionDriver;
import com.swifta.schoolportal.dblogic.JDCConnectionPool;
import com.swifta.schoolportal.utils.AppValues;
import com.swifta.schoolportal.utils.PortalDatabase;
import com.swifta.schoolportal.utils.UserAuthentication;
import java.io.FileNotFoundException;
import java.util.Properties;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import main.PropertyFileReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
@ManagedBean
@ApplicationScoped
public class PortalInitBean {

    /**
     * Creates a new instance of PortalInitBean
     */
    private static Logger logger = Logger.getLogger(PortalInitBean.class);

    public PortalInitBean() {
    }

    static {
        try {
            Properties prop = new PropertyFileReader(new UserAuthentication().getFilePath()).getAllProperties();

            PortalInitBean.logger.info("Reading database parameters .... Started ... ");
            PortalDatabase.driver = prop.getProperty("driver");
            PortalInitBean.logger.info("Driver : " + PortalDatabase.driver);
            PortalDatabase.url = prop.getProperty("url");
            PortalInitBean.logger.info("URL : " + PortalDatabase.url);
            PortalDatabase.username = prop.getProperty("username");
            PortalInitBean.logger.info("Username : " + PortalDatabase.username);
            PortalDatabase.password = prop.getProperty("password");
            PortalInitBean.logger.info("Password : " + PortalDatabase.password);

            PortalInitBean.logger.info("Reading database production parameters .... Ended ... ");

            //timeout
            AppValues.timeOut = Integer.valueOf(prop.getProperty("time_out"));
            PortalInitBean.logger.info("Session timeout : " + AppValues.timeOut + " seconds");

            //pin length
            AppValues.pinLength = Integer.valueOf(prop.getProperty("pin_length"));
            PortalInitBean.logger.info("Pin length : " + AppValues.pinLength + "");

            //sms gateway ip and port
            AppValues.gatewayAddress = prop.getProperty("ip-address");
            PortalInitBean.logger.info("Gateway Address : " + AppValues.gatewayAddress);
            AppValues.gatewayPort = prop.getProperty("ip-port");
            PortalInitBean.logger.info("Gateway Port : " + AppValues.gatewayPort);

            AppValues.gatewayTimeOut = Integer.parseInt(prop.getProperty("gateway-timeout"));
            PortalInitBean.logger.info("Gateway Time Out : " + AppValues.gatewayTimeOut);


            //connection pool
            String pool = prop.getProperty("max-pool");
            PortalInitBean.logger.info("Max Pool Limit : " + pool);
            AppValues.maxPool = Integer.parseInt(pool.trim());

            pool = prop.getProperty("min-pool");
            PortalInitBean.logger.info("Min Pool Limit : " + pool);
            AppValues.minPool = Integer.parseInt(pool.trim());

            pool = prop.getProperty("max-size");
            PortalInitBean.logger.info("Max Pool : " + pool);
            AppValues.maxSize = Integer.parseInt(pool.trim());

            pool = prop.getProperty("idle-timeout");
            PortalInitBean.logger.info("Idle Timeout : " + pool);
            AppValues.idleTimeout = Integer.parseInt(pool.trim());


            //create database connection pool
            PortalInitBean.logger.info("Creating database connection pool");

            JDCConnectionDriver jdc = new JDCConnectionDriver(PortalDatabase.driver, PortalDatabase.url, PortalDatabase.username, PortalDatabase.password);
            PortalDatabase.source = new JDCConnectionPool(PortalDatabase.url, PortalDatabase.username, PortalDatabase.password);

            PortalInitBean.logger.info("Creating database connection pool ... Done");

        } catch (FileNotFoundException ex) {
            PortalInitBean.logger.error(ex);
        } catch (Exception ex) {
            PortalInitBean.logger.error(ex);
            ex.printStackTrace();
        }
    }

    public static void initApp() {
        PortalInitBean.logger.info("Initialising application ... .");
    }
}
