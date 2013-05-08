/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.datamodel.StudentDataModel;
import com.swifta.schoolportal.datamodel.TransactionHistoryDataModel;
import com.swifta.schoolportal.dblogic.AuditTrailDatabase;
import com.swifta.schoolportal.dblogic.PortalAdminDatabase;
import com.swifta.schoolportal.dblogic.SchoolDatabase;
import com.swifta.schoolportal.dblogic.StudentDatabase;
import com.swifta.schoolportal.dblogic.TransactionHistoryDatabase;
import com.swifta.schoolportal.entities.*;
import com.swifta.schoolportal.utils.AppValues;
import com.swifta.schoolportal.utils.NumberUtilities;
import com.swifta.schoolportal.utils.PageUrls;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Logger;
//import org.primefaces.event.CellEditEvent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.event.TabChangeEvent;

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
    private AuditTrailDatabase auditTrailDatabase = new AuditTrailDatabase();
    private SchoolDatabase schoolDatabase = new SchoolDatabase();
    private StudentDatabase studentDatabase = new StudentDatabase();
    private Logger logger = Logger.getLogger(SchoolBean.class);
    private List<Student> students;
    private List<TransactionHistory> histories, schoolPayHistories;
    private List<AuditTrail> auditTrails;
    private School selectedSchool;
    private SchoolAdmin selectedAdmin;
    private PortalSession portalSession;
    private School school;
    private PaymentMode paymentMode;
    private PartnerService partnerService;
    private PortalAdmin portalAdmin, selectedPortalAdmin;
    private AdminRole adminRole;
    private Student student, selectedStudent;
    private StudentDataModel studentDataModel;
    private int studentId, activeIndex = 0;
    private Date firstDate = new Date(), secondDate = new Date();
    private AppValues appValues = null;
    private List<String> selectedActionsPerformed = null, selectedActionTypes = null;
    private String currentDate = "", fromDate = "", toDate = "", selectedAction = "", selectedActor = "", subTotal = "";
    private TransactionHistory[] selectedTransactionHistories;

    public SchoolBean() {
        admin = new SchoolAdmin();
        school = new School();
        student = new Student();
        selectedStudent = new Student();
        selectedAdmin = new SchoolAdmin();
        selectedSchool = new School();
        portalSession = new PortalSession();
        studentDataModel = new StudentDataModel(getStudents());
        portalAdmin = new PortalAdmin();
        selectedPortalAdmin = new PortalAdmin();
        adminRole = new AdminRole();
        portalSession.getAppSession().setAttribute("portal_admin_school_id", 0);
        fromDate = "";
        toDate = "";
        appValues = new AppValues();
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public TransactionHistory[] getSelectedTransactionHistories() {
        return selectedTransactionHistories;
    }

    public void setSelectedTransactionHistories(TransactionHistory[] selectedTransactionHistories) {
        this.selectedTransactionHistories = selectedTransactionHistories;
    }

    public List<String> getSelectedActionsPerformed() {
        return selectedActionsPerformed;
    }

    public void setSelectedActionsPerformed(List<String> selectedActionsPerformed) {
        this.selectedActionsPerformed = selectedActionsPerformed;
    }

    public List<String> getSelectedActionTypes() {
        return selectedActionTypes;
    }

    public void setSelectedActionTypes(List<String> selectedActionTypes) {
        this.selectedActionTypes = selectedActionTypes;
    }

    public Date getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(Date firstDate) {
        this.firstDate = firstDate;
    }

    public Date getSecondDate() {
        return secondDate;
    }

    public void setSecondDate(Date secondDate) {
        this.secondDate = secondDate;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(String selectedAction) {
        this.selectedAction = selectedAction;
    }

    public String getSelectedActor() {
        return selectedActor;
    }

    public void setSelectedActor(String selectedActor) {
        this.selectedActor = selectedActor;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getCurrentDate() {
        return new Date().toString();
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public PortalAdmin getSelectedPortalAdmin() {
        return selectedPortalAdmin;
    }

    public void setSelectedPortalAdmin(PortalAdmin selectedPortalAdmin) {
        this.selectedPortalAdmin = selectedPortalAdmin;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(AdminRole adminRole) {
        this.adminRole = adminRole;
    }

    public PortalAdmin getPortalAdmin() {
        return portalAdmin;
    }

    public void setPortalAdmin(PortalAdmin portalAdmin) {
        this.portalAdmin = portalAdmin;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public SchoolAdmin getSelectedAdmin() {
        return selectedAdmin;
    }

    public void setSelectedAdmin(SchoolAdmin selectedAdmin) {
        this.selectedAdmin = selectedAdmin;
    }

    public School getSelectedSchool() {
        return selectedSchool;
    }

    public void setSelectedSchool(School selectedSchool) {
        this.selectedSchool = selectedSchool;
    }

    public void retrieveSelectedSchool(String newValue) {
        portalSession.getAppSession().setAttribute("portal_admin_school_id", newValue);
        logger.info("-----------------------the portal session selected school Id is >>>>>>>>>>>>>>>>new value= " + newValue + " : old value= ");
    }

    public void redeemSelectedTransactions() {
        logger.info("------------redeem selected" + selectedTransactionHistories);
        int count = 0;
        TransactionHistoryDatabase txnDatabase = new TransactionHistoryDatabase();
        if (selectedTransactionHistories != null) {
            for (int i = 0; i < selectedTransactionHistories.length; i++) {
                try {
                    txnDatabase.updateTransactionHistory(selectedTransactionHistories[i].getId());
                    count++;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                logger.info("-----selected id :" + i + "  " + selectedTransactionHistories[i]);
            }
        }
        showMessage(count + " Transaction(s) marked as redeemed.");
   //     
    }

    public void redeemTransaction(String transactionId) {
        logger.info("---------------txn id====" + transactionId);
        try {
            new TransactionHistoryDatabase().updateTransactionHistory(Integer.valueOf(transactionId));
            showMessage("Transaction marked as redeemed.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deletePortalAdmin(String portalAdminId) {
        try {
            adminDatabase.deleteAdminRole(portalAdminId);
            adminDatabase.deletePortalAdmin(portalAdminId);
            PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
            logger.info("=====================create audit trail==================" + portalAdmin);
            createAuditTrail(appValues.AUDITTRAIL_DELETE, appValues.AUDITTRAIL_ADMINS_ENTITY, portalAdmin, null);
            showMessage("Portal admin deleted successfully");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("---------------------------display this message");
        showMessage("Portal admin deleted successfully");
    }

    public void retrievePortalAdmin(String portalAdminId) throws IOException {
        logger.info("its the admin id........................................////////////////////" + portalAdminId);
        //  this.selectedStudent = new Student();
        logger.info("its the admin .......................................////////////////////" + selectedPortalAdmin.toString());
        try {
            this.selectedPortalAdmin = adminDatabase.getAdminById(portalAdminId);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("its after the portal admin .......................................////////////////////" + selectedPortalAdmin.toString());
        portalSession.redirect(PageUrls.UPDATE_PORTAL_ADMIN);
    }

    public int getStudentId() {
        logger.info("-------------------student id >>>" + studentId);
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public List<TransactionHistory> getSchoolPayHistories() {
        return schoolPayHistories;
    }

    public void setSchoolPayHistories(List<TransactionHistory> schoolPayHistories) {
        this.schoolPayHistories = schoolPayHistories;
    }

    public SchoolAdmin getAdmin() {
        return admin;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent() {
        this.student = student;
    }

    public void retrieveStudent(String studentId) throws IOException {
        logger.info("its the student id........................................////////////////////" + studentId);
        //  this.selectedStudent = new Student();
        logger.info("its the student .......................................////////////////////" + selectedStudent.toString());
        try {
            this.selectedStudent = studentDatabase.getStudent(Long.valueOf(studentId));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("its after the student .......................................////////////////////" + selectedStudent.toString());
        portalSession.redirect(PageUrls.UPDATE_STUDENT_DATA);
    }

    public void redirectToRegisterStudents() throws IOException {
        portalSession.redirect(PageUrls.SCHOOL_MAIN);
    }

    public void redirectToManageAdmins() throws IOException {
        portalSession.redirect(PageUrls.ADMIN_MAIN);
    }

    public void redirectToManageSchools() throws IOException {
        logger.info("..........redirect to manage schools...............");
        portalSession.redirect(PageUrls.ADMIN_MAIN);
    }

    public String retrieveSchoolName(String schoolId) {
        String schoolName = "";
        School school = new School();
        logger.info("its the school id........................................////////////////////" + schoolId);
        //  this.selectedschool = new school();
        logger.info("its the student .......................................////////////////////" + school.toString());
        try {
            school = adminDatabase.getSchoolDetails(Integer.valueOf(schoolId));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("its the school after db retrival .......................................////////////////////" + school.toString());
        if (school != null) {
            schoolName = school.getName();
        };
        logger.info("its the school name .......................................////////////////////" + schoolName);
        return schoolName;
    }

    public void retrieveSchool(String schoolId) throws IOException {
        logger.info("its the school id........................................////////////////////" + schoolId);
        //  this.selectedschool = new school();
        logger.info("its the student .......................................////////////////////" + selectedSchool.toString());
        try {
            this.selectedSchool = adminDatabase.getSchoolDetails(Integer.valueOf(schoolId));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("its after the school .......................................////////////////////" + selectedSchool.toString());
        portalSession.redirect(PageUrls.UPDATE_SCHOOL_DATA);
    }

    public void retrieveAuditTrail() {
        Iterator defIter = null;
        if (selectedActionTypes != null) {
            defIter = selectedActionTypes.iterator();
            while (defIter.hasNext()) {

                logger.info("------------action type" + defIter.next());
            }
        }
        if (selectedActionsPerformed != null) {
            defIter = selectedActionsPerformed.iterator();
            while (defIter.hasNext()) {

                logger.info("------------action type" + defIter.next());
            }
        }
        logger.info("--------------from date: " + new Timestamp(firstDate.getTime()) + "==========to Date : " + new Timestamp(secondDate.getTime()));
        try {
            logger.info("---inside try catch");
            this.auditTrails = new AuditTrailDatabase().getAuditTrails(selectedActionsPerformed, null, null, new Timestamp(firstDate.getTime()).toString(), new Timestamp(secondDate.getTime()).toString(), selectedActionTypes);
            logger.info("---------after try catch");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void retrieveSchoolAdmin(String schoolAdminId) throws IOException {
        logger.info("its the schoolAdmin id........................................////////////////////" + schoolAdminId);
        //  this.selectedschoolAdmin = new schoolAdmin();
        logger.info("its the student .......................................////////////////////" + selectedAdmin.toString());
        try {
            this.selectedAdmin = adminDatabase.getSchoolAdminDetails(Integer.valueOf(schoolAdminId));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        logger.info("its after the schoolAdmin .......................................////////////////////" + selectedAdmin.toString());
        portalSession.redirect(PageUrls.UPDATE_ADMIN_DATA);
    }

    public Student getSelectedStudent() {
        return selectedStudent;
    }

    public void setSelectedStudent(Student selectedStudent) {
        this.selectedStudent = selectedStudent;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool() {
        this.school = school;
    }

    public PartnerService getPartnerService() {
        return partnerService;
    }

    public void setPartnerService(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public void setAdmin(SchoolAdmin admin) {
        this.admin = admin;
    }

    public List<Student> getStudents() {
        try {
            List<Student> allStudents = new ArrayList<Student>();
            String adminSchoolId = String.valueOf(portalSession.getAppSession().getAttribute("portal_admin_school_id"));
            if (!adminSchoolId.equals("null")) {
                allStudents = new SchoolDatabase().getAllSchoolStudents(Integer.parseInt(adminSchoolId));
            }
            return allStudents;
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return new ArrayList<Student>();
        }
    }

    public TransactionHistoryDataModel getTransacionHistoryModel() {
        selectedTransactionHistories = null;
        TransactionHistoryDataModel transacionHistoryModel = new TransactionHistoryDataModel(getHistories());
        return transacionHistoryModel;
    }

    public List<TransactionHistory> getHistories() {
        TransactionHistoryDatabase transactionHistoryDatabase = new TransactionHistoryDatabase();
        try {
            List<TransactionHistory> transactionHistoryList = new ArrayList<TransactionHistory>();
            transactionHistoryList = transactionHistoryDatabase.getHistory();
            this.subTotal = "Sub Total = $ " + new NumberUtilities().format(transactionHistoryDatabase.subTotal);
            return transactionHistoryList;
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
            return histories;
        }
    }

    public List<String> getAuditTrailActions() {
        return new ArrayList<String>();
    }

    public List<AuditTrail> getAuditTrails() {
        String actionPerformed = null, description = null, originatorName = null, dateFrom = "0000-00-00 00:00:00", dateTo = new Timestamp(new Date().getTime()).toString();
        if (this.auditTrails == null) {
            try {
                logger.info("----------------------before calling retrieval of audit trail");
                return new AuditTrailDatabase().getAuditTrails(null, description, originatorName, dateFrom, dateTo, null);
            } catch (SQLException ex) {
                logger.error(ex);
                ex.printStackTrace();
                return auditTrails;
            }
        } else {
            return auditTrails;
        }
    }

    public void setHistories(List<TransactionHistory> histories) {
        this.histories = histories;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public StudentDataModel getStudentDataModel() {
        return studentDataModel;
    }

    public void createAuditTrail(String actionPerformed, String entity, PortalAdmin admin, SchoolAdmin sAdmin) {
        String description = actionPerformed + " - on " + entity;
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setActionPerformed(actionPerformed);
        auditTrail.setDateCreated(new Timestamp(new Date().getTime()).toString());
        auditTrail.setDescription(description);
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        auditTrail.setOriginatorIpAddress(ipAddress);

        if (admin != null) {
            auditTrail.setOriginatorId(admin.getId());
            auditTrail.setOriginatorName(admin.getUsername());
            auditTrail.setOriginatorType(appValues.AUDITTRAIL_ADMIN);
        } else {
            auditTrail.setOriginatorName(sAdmin.getUsername());
            auditTrail.setOriginatorSchoolAdminId(sAdmin.getId());
            auditTrail.setOriginatorType(appValues.AUDITTRAIL_SCHOOLADMIN);
        }
        try {
            auditTrailDatabase.createAuditTrail(auditTrail);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void createAdmin() {
        if (validate(admin)) {
            try {
                if (!adminDatabase.existingSchoolAdmin(admin)) {
                    if (!adminDatabase.createSchoolAdmin(admin)) {
                        PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                        logger.info("=====================create audit trail==================" + portalAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_CREATE, appValues.AUDITTRAIL_SCHOOLADMIN_ENTITY, portalAdmin, null);
                        logger.info("The admin school id is " + admin.getSchoolID());
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

    public void createPortalAdmin() {
        if (validate(portalAdmin)) {
            try {
                if (!adminDatabase.existingPortalAdmin(portalAdmin)) {
                    if (!adminDatabase.createPortalAdmin(portalAdmin)) {
                        PortalAdmin newPortalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                        logger.info("=====================create audit trail==================" + newPortalAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_CREATE, appValues.AUDITTRAIL_ADMINS_ENTITY, newPortalAdmin, null);
                        logger.info("The admin role id is =======================================" + portalAdmin.getRoleId());
                        showMessage("New Portal Admin created ... ");
                        portalAdmin = new PortalAdmin();
                    }
                } else {
                    showMessage("Existing portal admin found in the records ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                portalAdmin = new PortalAdmin();
                ex.printStackTrace();
            }
        }
    }

    public void createSchool() {
        logger.info("Afer create school " + school.toString());
        if (validate(school)) {
            logger.info("after validation");
            try {
                //hard coded ids....prone to fail if the id of the default payment methods fail.
                school.setPartnerServiceId("1");
                school.setPaymentModeId("1");
                if (!schoolDatabase.existingSchool(school)) {
                    if (!schoolDatabase.createSchool(school)) {
                        PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                        logger.info("=====================create audit trail==================" + portalAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_CREATE, appValues.AUDITTRAIL_PARTNERSERVICEUNIT_ENTITY, portalAdmin, null);
                        logger.info("--------------------------------after creating school");
                        showMessage("New School created ... ");
                        school = new School();
                    }
                } else {
                    showMessage("Existing school found in the records ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                school = new School();
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
    }

    public void createStudent(String schoolName) {
        logger.info("Afer create student " + student.toString());
        if (validate(student)) {
            logger.info("after validation");
            try {
                if (!studentDatabase.existingStudent(student, schoolName)) {
                    if (!studentDatabase.createStudent(student)) {
                        SchoolAdmin schoolAdmin = (SchoolAdmin) portalSession.getAppSession().getAttribute("logged_in_school_admin");
                        logger.info("=====================create audit trail==================" + schoolAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_CREATE, appValues.AUDITTRAIL_PERSONINFO_ENTITY, null, schoolAdmin);
                        showMessage("New Student created ... ");
                        student = new Student();
                    }
                } else {
                    showMessage("Existing student found in the records ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                student = new Student();
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
    }

    public void updateStudent() {
        logger.info("Afer create student " + selectedStudent.toString());
        if (validateUpdate(selectedStudent)) {
            logger.info("after validation");
            try {
                if (!studentDatabase.existingStudent(selectedStudent)) {
                    if (!studentDatabase.updateStudent(selectedStudent)) {
                        SchoolAdmin schoolAdmin = (SchoolAdmin) portalSession.getAppSession().getAttribute("logged_in_school_admin");
                        logger.info("=====================create audit trail==================" + schoolAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_UPDATE, appValues.AUDITTRAIL_PERSONINFO_ENTITY, null, schoolAdmin);
                        showMessage("Student data updated ... ");
                    }
                } else {
                    showMessage("Existing student found in the records with same registration number ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
    }

    public void onTabChange(TabChangeEvent event) {
        logger.info("tab id = " + event.getTab().getId());
        String tabId = event.getTab().getId();
        if (tabId.equalsIgnoreCase("stdreg")) {
            this.activeIndex = 0;
        } else if (tabId.equalsIgnoreCase("regstd")) {
            this.activeIndex = 1;
        } else if (tabId.equalsIgnoreCase("tranxhist")) {
            this.activeIndex = 2;
        } else if (tabId.equalsIgnoreCase("manageschool")) {
            this.activeIndex = 0;
        } else if (tabId.equalsIgnoreCase("manageportaladmin")) {
            this.activeIndex = 1;
        } else if (tabId.equalsIgnoreCase("manageadmin")) {
            this.activeIndex = 2;
        } else if (tabId.equalsIgnoreCase("transhist")) {
            this.activeIndex = 3;
        } else if (tabId.equalsIgnoreCase("audittrail")) {
            this.activeIndex = 4;
        }
    }

    public void updatePortalAdmin() {
        if (validate(selectedPortalAdmin)) {
            logger.info("after validation");
            try {
                if (!adminDatabase.existingPortalAdmin(selectedPortalAdmin)) {
                    if (!adminDatabase.updatePortalAdmin(selectedPortalAdmin)) {
                        PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                        logger.info("=====================create audit trail==================" + portalAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_UPDATE, appValues.AUDITTRAIL_ADMINS_ENTITY, portalAdmin, null);
                        showMessage("admin data updated ... ");
                    }
                } else {
                    showMessage("Existing admin found in the records; confirm that the code or name doesn't exit... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
    }

    public void updateSchool() {
        if (validate(selectedSchool)) {
            logger.info("after validation");
            try {
                if (!schoolDatabase.existingSchool(selectedSchool)) {
                    if (!schoolDatabase.updateSchool(selectedSchool)) {
                        PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                        logger.info("=====================create audit trail==================" + portalAdmin);
                        createAuditTrail(appValues.AUDITTRAIL_UPDATE, appValues.AUDITTRAIL_PARTNERSERVICEUNIT_ENTITY, portalAdmin, null);
                        showMessage("School data updated ... ");
                    }
                } else {
                    showMessage("Existing school found in the records; confirm that the code or name doesn't exit... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
    }

    public void updateSchoolAdmin() {
        if (validate(selectedAdmin)) {
            logger.info("after validation");
            try {
                if (!adminDatabase.updateAdmin(selectedAdmin)) {
                    PortalAdmin portalAdmin = (PortalAdmin) portalSession.getAppSession().getAttribute("logged_in_portal_admin");
                    logger.info("=====================create audit trail==================" + portalAdmin);
                    createAuditTrail(appValues.AUDITTRAIL_UPDATE, appValues.AUDITTRAIL_SCHOOLADMIN_ENTITY, portalAdmin, null);
                    showMessage("School admin data updated ... ");
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        }
        logger.info("validation failed....");
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

    /*
     * this method should be uncommented if the section for selecting payment
     * mode and partner service is uncommented private boolean validate(School
     * school) { if (school.getName() != null) { logger.info("school name not
     * null...."); if (school.getName().length() > 0) { logger.info("school name
     * valid...."); if (school.getPartnerServiceId().length() > 0) {
     * logger.info("school partner service valid...."); if
     * (school.getPaymentModeId().length() > 0) { logger.info("payment mode
     * valid...."); return true; } else { showMessage("Select a payment
     * mode..."); return false; } } else { showMessage("Select a partner
     * service..."); return false; } } else { showMessage("Name of school
     * required ... "); return false; } } else { showMessage("Name of school
     * required ... "); return false; } }
     */
    private boolean validate(School school) {
        if (school.getName() != null) {
            logger.info("school name not null....");
            if (school.getName().length() > 0) {
                logger.info("school name valid....");
                if (school.getSchoolCode().length() > 0) {
                    logger.info("school code valid....");
                    if (school.getSchoolCode().length() < 3) {
                        logger.info("school code length valid....");
                        return true;
                    } else {
                        showMessage("School registration code exceeds 2 characters ... ");
                        return false;
                    }
                } else {
                    showMessage("School registration code required ... ");
                    return false;
                }
            } else {
                showMessage("Name of school required ... ");
                return false;
            }
        } else {
            showMessage("Name of school required ... ");
            return false;
        }
    }

    private boolean validate(Student student) {
        logger.info("student name not null....");
        if (student.getName() != null && student.getName().length() > 0) {
            logger.info("student name valid....");
            if (student.getIdentNo() != null && student.getIdentNo().length() > 0) {
                logger.info("student id number valid....");
                return true;
            } else {
                showMessage("Registration number required...");
                return false;
            }
        } else {
            showMessage("Name of student required ... ");
            return false;
        }
    }

    private boolean validate(PortalAdmin portalAdmin) {
        logger.info("portal admin username not null....");
        if (portalAdmin.getUsername() != null && portalAdmin.getUsername().length() > 0) {
            logger.info("portal admin username valid....");
            if (portalAdmin.getPassword() != null && portalAdmin.getPassword().length() > 0) {
                logger.info("password is valid....");
                return true;
            } else {
                showMessage("Password required...");
                return false;
            }
        } else {
            showMessage("Username of portal admin required ... ");
            return false;
        }
    }

    private boolean validateUpdate(Student student) {
        logger.info("student name not null....");
        if (student.getName() != null && student.getName().length() > 0) {
            logger.info("student name valid....");
            if (student.getIdentNo() != null && student.getIdentNo().length() > 0) {
                logger.info("student id number valid...." + student.getSchoolCode());
                if (student.getIdentNo().toLowerCase().startsWith(student.getSchoolCode().toLowerCase())) {
                    return true;
                } else {
                    showMessage("Registration number does not conform to the expected format. Missing correct school registration code...");
                    return false;
                }
            } else {
                showMessage("Registration number required...");
                return false;
            }
        } else {
            showMessage("Name of student required ... ");
            return false;
        }
    }

    private void showMessage(String message) {
        FacesMessage fm = new FacesMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }
}