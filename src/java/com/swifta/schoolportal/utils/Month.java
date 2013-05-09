/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.utils;

import com.swifta.schoolportal.datamodel.TransactionHistoryDataModel;

/**
 *
 * @author princeyekaso
 */
public class Month {

    private int id = 0;
    private String name = "",monthSubtotal = "";
    private TransactionHistoryDataModel transactionHistoryDataModel;

    public Month(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getMonthSubtotal() {
        return monthSubtotal;
    }

    public void setMonthSubtotal(String monthSubtotal) {
        this.monthSubtotal = monthSubtotal;
    }

    public TransactionHistoryDataModel getTransactionHistoryDataModel() {
        return transactionHistoryDataModel;
    }

    public void setTransactionHistoryDataModel(TransactionHistoryDataModel transactionHistoryDataModel) {
        this.transactionHistoryDataModel = transactionHistoryDataModel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
