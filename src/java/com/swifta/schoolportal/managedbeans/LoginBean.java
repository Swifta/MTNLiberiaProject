/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.dblogic.SchoolDatabase;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.utils.*;
import java.io.IOException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
@ManagedBean
@SessionScoped
public class LoginBean {

    /**
     * Creates a new instance of LoginBean
     */
    private String username, password, pinSent;
    private Logger logger = Logger.getLogger(LoginBean.class);
    private long timeSent, timeReceived;
    private String errorString;
    private PortalSession portalSession;
    private SchoolAdmin admin;

    public LoginBean() {
        portalSession = new PortalSession();
        errorString = "";
    }

    public SchoolAdmin getAdmin() {
        return admin;
    }

    public void setAdmin(SchoolAdmin admin) {
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    public void sendPin() {
        try {

            admin = new SchoolDatabase().getSchoolAdminDetails(username);

            if (admin != null) {
                //send pin to username
                SendUserPin msg = new SendUserPin();
                String pin = new UsernameGenerator().generatePin(AppValues.pinLength);

                logger.info("PIN : " + pin);
                boolean sent = msg.sendPin(pin, new MsisdnValidator().transform(admin.getPhoneNo()));
                //boolean sent = true;
                if (sent) {
                    timeSent = System.currentTimeMillis();
                    pinSent = pin;
                    portalSession.redirect(PageUrls.PIN_PAGE);
                } else {
                    setErrorString("Error while sending PIN. Please try again later.");
                }
            } else {
                setErrorString("Unable to validate username and/or password");
            }

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            setErrorString("An error occured while validationg your details. Please try again later.");
        }
    }

    public void validatePin() {
        logger.info("Validating pin "+password);
        if (password.length() > 0) {
            if (password.equals(pinSent)) {
                try {
                    portalSession.getAppSession().setAttribute("portal_admin_school", admin.getSchoolName());
                    portalSession.getAppSession().setAttribute("portal_admin_school_id", admin.getSchoolID());
                    portalSession.getAppSession().setAttribute("portal_admin_email", admin.getEmailAddress());
                    portalSession.getAppSession().setAttribute("portal_admin_fname", admin.getFirstName());
                    portalSession.getAppSession().setAttribute("portal_admin_lname", admin.getLastName());
                    errorString = "";
                    pinSent = "";
                    password = "";
                    username = "";
                    portalSession.redirect(PageUrls.SCHOOL_MAIN);
                } catch (IOException ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            } else {
                setErrorString("Invalid PIN entered. Please try again.");
            }
        } else {
            setErrorString("Please enter the PIN sent to your mobile.");
        }
    }

    public void logOut() {
        try {
            portalSession.redirect(0);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    public void goBack() {
        try {
            portalSession.redirect(0);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
