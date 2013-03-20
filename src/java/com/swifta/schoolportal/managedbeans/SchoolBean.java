/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.dblogic.PortalAdminDatabase;
import com.swifta.schoolportal.dblogic.SchoolDatabase;
import com.swifta.schoolportal.entities.SchoolAdmin;
import com.swifta.schoolportal.entities.Student;
import com.swifta.schoolportal.entities.TransactionHistory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Opeyemi
 */
@ManagedBean
@SessionScoped
public class SchoolBean {

    /**
     * Creates a new instance of SchoolBean
     */
    private SchoolAdmin admin;
    private PortalAdminDatabase adminDatabase = new PortalAdminDatabase();
    private Logger logger = Logger.getLogger(PortalAdminBean.class);
    private List<Student> students;
    private List<TransactionHistory> histories;
    private PortalSession portalSession;
    
    public SchoolBean() {
        admin = new SchoolAdmin();
        portalSession = new PortalSession();
    }
    
    public SchoolAdmin getAdmin() {
        return admin;
    }
    
    public void setAdmin(SchoolAdmin admin) {
        this.admin = admin;
    }

    public List<Student> getStudents() {
        try {
            return new SchoolDatabase().getAllSchoolStudents(Integer.parseInt(String.valueOf(portalSession.getAppSession().getAttribute("portal_admin_school_id"))));
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return new ArrayList<Student>();
        }
    }

    public List<TransactionHistory> getHistories() {
        try {
            return new SchoolDatabase().getHistory();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return histories;
        }
    }

    public void setHistories(List<TransactionHistory> histories) {
        this.histories = histories;
    }    

    public void setStudents(List<Student> students) {
        this.students = students;
    }    
    
    
    public void createAdmin() {
        if (validate(admin)) {
            try {
                if (!adminDatabase.existingSchoolAdmin(admin)) {
                    if (!adminDatabase.createSchoolAdmin(admin)) {
                        showMessage("New School Admin created ... ");
                        admin = new SchoolAdmin();
                    }
                } else {
                    showMessage("Existing school admin found in the records ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                admin = new SchoolAdmin();
                ex.printStackTrace();
            }
        }
    }
    
    private boolean validate(SchoolAdmin admin) {
        if (admin.getFirstName().length() > 0) {
            if (admin.getLastName().length() > 0) {
                if (admin.getEmailAddress().length() > 0) {
                    if (admin.getPhoneNo().length() > 0) {
                        return true;
                    } else {
                        showMessage("Mobile Number required ... ");
                        return false;
                    }
                } else {
                    showMessage("Email address required ... ");
                    return false;
                }
            } else {
                showMessage("Last Name required ... ");
                return false;
            }
        } else {
            showMessage("First Name required ... ");
            return false;
        }
    }
    
    private void showMessage(String message) {
        FacesMessage fm = new FacesMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }
}
