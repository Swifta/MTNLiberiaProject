/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.datamodel;

import com.swifta.schoolportal.dblogic.TransactionHistoryDatabase;
import com.swifta.schoolportal.entities.TransactionHistory;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.ListDataModel;
import org.apache.log4j.Logger;
import org.primefaces.model.SelectableDataModel;

/**
 *
 * @author princeyekaso
 */
public class TransactionHistoryDataModel extends ListDataModel<TransactionHistory> implements SelectableDataModel<TransactionHistory> {

    private Logger logger = Logger.getLogger(TransactionHistoryDataModel.class);

    public TransactionHistoryDataModel() {
    }

    public TransactionHistoryDataModel(List<TransactionHistory> data) {
        super(data);
    }

    @Override
    public TransactionHistory getRowData(String rowKey) {
        try {
            //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  

            return new TransactionHistoryDatabase().getTransactionHistoryById(Integer.valueOf(rowKey));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Object getRowKey(TransactionHistory transactionHistory) {
        return transactionHistory.getId();
    }

    /*  public static void setValue2ValueExpression(final Object value, final String expression) {
     FacesContext facesContext = FacesContext.getCurrentInstance();
     ELContext elContext = facesContext.getELContext();

     ValueExpression targetExpression =
     facesContext.getApplication().getExpressionFactory().createValueExpression(elContext, expression, Object.class);
     targetExpression.setValue(elContext, value);
     }*/
}
