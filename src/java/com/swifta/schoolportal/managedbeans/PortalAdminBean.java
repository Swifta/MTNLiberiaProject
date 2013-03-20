/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.dblogic.PortalAdminDatabase;
import com.swifta.schoolportal.entities.PortalAdmin;
import com.swifta.schoolportal.entities.School;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.utils.PageUrls;
import com.swifta.schoolportal.utils.UserAuthentication;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
@ManagedBean
@SessionScoped
public class PortalAdminBean {

    /**
     * Creates a new instance of PortalAdminBean
     */
    private PortalAdmin portalAdmin;
    private Logger logger = Logger.getLogger(PortalAdminBean.class);
    private String message;
    private PortalSession portalSession;
    private List<School> schools;
    private PortalAdminDatabase adminDatabase = new PortalAdminDatabase();
    private List<SchoolAdmin> admins;
    
    public PortalAdminBean() {
        portalAdmin = new PortalAdmin();
        portalSession = new PortalSession();
        message = "";
    }
    
    public PortalAdmin getPortalAdmin() {
        return portalAdmin;
    }
    
    public void setPortalAdmin(PortalAdmin portalAdmin) {
        this.portalAdmin = portalAdmin;
    }
    
    public List<SchoolAdmin> getAdmins() {
        try {
            return adminDatabase.getAllAdmins();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return admins;
        }
    }
    
    public void setAdmins(List<SchoolAdmin> admins) {
        this.admins = admins;
    }
    
    public List<School> getSchools() {
        try {
            return adminDatabase.getAllSchools();
        } catch (SQLException ex) {
            logger.error(ex);
            return new ArrayList<School>();
        }
    }
    
    public void setSchools(List<School> schools) {
        this.schools = schools;
    }
    
    public void authenticate() {
        try {
            UserAuthentication auth = new UserAuthentication();
            logger.info("Authenticating ... ");
            logger.info("Username : " + portalAdmin.getUsername());
            logger.info("Password : " + portalAdmin.getPassword());
            if (auth.authenticateAdmin(portalAdmin.getUsername(), portalAdmin.getPassword())) {
                message = "";
                logger.info("Admin authenticated ... ");
                portalSession.redirect(PageUrls.ADMIN_MAIN);
            } else {
                logger.info("Invalid username and/or password");
                message = "Invalid username and/or password";
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void logOut() {
        try {
            portalSession.redirect(0);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
