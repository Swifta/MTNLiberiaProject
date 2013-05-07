/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.dblogic.PartnerServiceDatabase;
import com.swifta.schoolportal.dblogic.PaymentModeDatabase;
import com.swifta.schoolportal.dblogic.PortalAdminDatabase;
import com.swifta.schoolportal.entities.*;
import com.swifta.schoolportal.utils.PageUrls;
import com.swifta.schoolportal.utils.UserAuthentication;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
    private List<PartnerService> partnerServices;
    private List<PaymentMode> paymentModes;

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }
    private String editable = "none";

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

    public void setPartnerServices(List<PartnerService> partnerServices) {
        this.partnerServices = partnerServices;
    }

    public List<PartnerService> getPartnerServices() {
        try {
            return new PartnerServiceDatabase().getAllPartnerServices();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return new ArrayList<PartnerService>();
        }
    }

    public void setPaymentModes(List<PaymentMode> paymentModes) {
        this.paymentModes = paymentModes;
    }

    public List<PaymentMode> getPaymentModes() {

        try {
            return new PaymentModeDatabase().getAllPaymentModes();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();

            return new ArrayList<PaymentMode>();
        }
    }

    public List<PortalAdmin> getPortalAdmins() {
        try {
            return adminDatabase.getAllPortalAdmins();
        } catch (SQLException ex) {
            logger.error(ex);
            return new ArrayList<PortalAdmin>();
        }
    }

    public List<AdminRole> getAdminRoles() {
        try {
            return adminDatabase.getAllAdminRoles();
        } catch (SQLException ex) {
            logger.error(ex);
            return new ArrayList<AdminRole>();
        }
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

    public void assignAdminRole(String username) {
        String assignedRole = "";
        try {
            assignedRole = adminDatabase.getAdminRoleName(username);
        } catch (SQLException ex) {
            logger.error(ex);
        }
        if (assignedRole.isEmpty()) {
            logger.info("The logged in user is not assigned a role ------------- default role assigned");
            this.editable = "block";
        } else if (assignedRole.equalsIgnoreCase("admin")) {
            this.editable = "none";
        } else if (assignedRole.equalsIgnoreCase("superadmin")) {
            this.editable = "block";
        }
    }

    public void authenticate() {
        try {
            UserAuthentication auth = new UserAuthentication();
            PortalAdmin admin = null;
            logger.info("Authenticating ... ");
            logger.info("Username : " + portalAdmin.getUsername());
            logger.info("Password : " + portalAdmin.getPassword());
            portalSession.getAppSession().setAttribute("logged_in_portal_admin", admin);
            portalSession.getAppSession().setAttribute("logged_in_school_admin", null);
            if ((admin = auth.authenticateAdmin(portalAdmin.getUsername(), portalAdmin.getPassword())) != null) {
                message = "";
                logger.info("Admin authenticated ... ");
                portalSession.redirect(PageUrls.ADMIN_MAIN);
                assignAdminRole(portalAdmin.getUsername());
                portalSession.getAppSession().setAttribute("logged_in_portal_admin", admin);
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
