/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.datamodel;

import com.swifta.schoolportal.dblogic.JDCConnection;
import com.swifta.schoolportal.entities.Student;
import com.swifta.schoolportal.utils.PortalDatabase;
import java.sql.ResultSet;
import java.util.List;
import javax.faces.model.ListDataModel;
import org.apache.log4j.Logger;
import org.primefaces.model.SelectableDataModel;
import java.sql.SQLException;

/**
 *
 * @author princeyekaso
 */
public class StudentDataModel extends ListDataModel<Student> implements SelectableDataModel<Student> {

    private Logger logger = Logger.getLogger(StudentDataModel.class);

    public StudentDataModel() {
    }

    public StudentDataModel(List<Student> data) {
        super(data);
    }

    @Override
    public Student getRowData(String rowKey) {
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  

        String sqlQuery = "select * from person_info where lower(identification_no) = '" + rowKey.toLowerCase() + "'";
        Student student = new Student();
        try {
            JDCConnection connection = PortalDatabase.source.getConnection();
            ResultSet res = connection.createStatement().executeQuery(sqlQuery);

            while (res.next()) {
                student.setName(res.getString("name"));
                student.setIdentNo(res.getString("identification_no"));
                student.setLastUpdate(res.getString("last_update"));
                student.setId(res.getInt("id"));
            }
            PortalDatabase.source.returnConnection(connection);
        } catch (SQLException sqlex) {
        }
        logger.info("Student Name : >>>>>>>>>>>" + student.toString());
        //connection.close();
     //   setValue2ValueExpression(student, "#{schoolBean.selectedStudent}");
        return student;
    }

    @Override
    public Object getRowKey(Student student) {
        return student.getIdentNo();
    }

  /*  public static void setValue2ValueExpression(final Object value, final String expression) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        ValueExpression targetExpression =
                facesContext.getApplication().getExpressionFactory().createValueExpression(elContext, expression, Object.class);
        targetExpression.setValue(elContext, value);
    }*/
}
