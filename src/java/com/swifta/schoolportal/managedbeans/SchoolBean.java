/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.managedbeans;

import com.swifta.schoolportal.datamodel.StudentDataModel;
import com.swifta.schoolportal.dblogic.PortalAdminDatabase;
import com.swifta.schoolportal.dblogic.SchoolDatabase;
import com.swifta.schoolportal.dblogic.StudentDatabase;
import com.swifta.schoolportal.entities.*;
import com.swifta.schoolportal.utils.PageUrls;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Logger;
//import org.primefaces.event.CellEditEvent;
import javax.faces.context.FacesContext;
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
    private SchoolDatabase schoolDatabase = new SchoolDatabase();
    private StudentDatabase studentDatabase = new StudentDatabase();
    private Logger logger = Logger.getLogger(SchoolBean.class);
    private List<Student> students;
    private List<TransactionHistory> histories;
    private PortalSession portalSession;
    private School school;
    private PaymentMode paymentMode;
    private PartnerService partnerService;
    private Student student, selectedStudent;
    private StudentDataModel studentDataModel;
    private int studentId, activeIndex = 0;

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
    private School selectedSchool;
    private SchoolAdmin selectedAdmin;

    public int getStudentId() {
        logger.info("-------------------student id >>>" + studentId);
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public SchoolBean() {
        admin = new SchoolAdmin();
        school = new School();
        student = new Student();
        selectedStudent = new Student();
        selectedAdmin = new SchoolAdmin();
        selectedSchool = new School();
        portalSession = new PortalSession();
        studentDataModel = new StudentDataModel(getStudents());
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
        portalSession.redirect(PageUrls.ADMIN_MAIN);
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

    public StudentDataModel getStudentDataModel() {
        return studentDataModel;
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

    public void createSchool() {
        logger.info("Afer create school " + school.toString());
        if (validate(school)) {
            logger.info("after validation");
            try {
                //hard coded ids....prone to fail if the id of the default payment methods fail.
                school.setPartnerServiceId("1");
                school.setPaymentModeId("1");
                if (!schoolDatabase.existingSchool(school.getName())) {
                    if (!schoolDatabase.createSchool(school)) {
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
                if (!studentDatabase.existingStudent(student)) {
                    if (!studentDatabase.createStudent(student, schoolName)) {
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
        if (validate(selectedStudent)) {
            logger.info("after validation");
            try {
                if (!studentDatabase.updateStudent(selectedStudent)) {
                    showMessage("Student data updated ... ");
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
        } else if (tabId.equalsIgnoreCase("transhist")) {
            this.activeIndex = 2;
        } else if (tabId.equalsIgnoreCase("manageschool")) {
            this.activeIndex = 0;
        } else if (tabId.equalsIgnoreCase("manageadmin")) {
            this.activeIndex = 1;
        }
    }

    public void updateSchool() {
        if (validate(selectedSchool)) {
            logger.info("after validation");
            try {
                if (!schoolDatabase.updateSchool(selectedSchool)) {
                    showMessage("School data updated ... ");
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
                return true;
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

    private void showMessage(String message) {
        FacesMessage fm = new FacesMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }
}