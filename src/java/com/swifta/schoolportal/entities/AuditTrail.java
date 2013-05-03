/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.entities;

/**
 *
 * @author princeyekaso
 */
public class AuditTrail {

    private int id = 0,originatorId=0;
    private String originatorName, actionPerformed, dateCreated, description, originatorIpAddress;

    public int getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(int originatorId) {
        this.originatorId = originatorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginatorName() {
        return originatorName;
    }

    public void setOriginatorName(String originatorName) {
        this.originatorName = originatorName;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginatorIpAddress() {
        return originatorIpAddress;
    }

    public void setOriginatorIpAddress(String originatorIpAddress) {
        this.originatorIpAddress = originatorIpAddress;
    }
}
