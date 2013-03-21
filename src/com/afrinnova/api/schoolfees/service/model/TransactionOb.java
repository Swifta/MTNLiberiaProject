/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.afrinnova.api.schoolfees.service.model;

/**
 *
 * @author mAladeojebi
 */
public class TransactionOb {
    
    private String acctref;
    private String statuscode;
    private long amount;

    
    public TransactionOb() {
    }
    
    

    public String getAcctref() {
        return acctref;
    }

    public void setAcctref(String acctref) {
        this.acctref = acctref;
    }

    public String getStatuscode() {
        return statuscode;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }
    
    public TransactionOb(String acctref,String statuscode, long amount){
        
        this.acctref = acctref;
        this.statuscode = statuscode;
        this.amount = amount;
    }
    
    
}
