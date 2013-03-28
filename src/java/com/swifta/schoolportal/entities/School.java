/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.entities;

/**
 *
 * @author Opeyemi
 */
public class School {

    private String name;
    private String paymentModeType;
    private String partnerServiceName;
    private int id;
    private String paymentModeId;
    private String partnerServiceId;

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

    public String getPaymentModeType() {
        return paymentModeType;
    }

    public void setPaymentModeType(String paymentModeType) {
        this.paymentModeType = paymentModeType;
    }

    public String getPartnerServiceName() {
        return partnerServiceName;
    }

    public void setPartnerServiceName(String PartnerServiceName) {
        this.partnerServiceName = partnerServiceName;
    }

    public String getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(String paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getPartnerServiceId() {
        return partnerServiceId;
    }

    public void setPartnerServiceId(String partnerServiceId) {
        this.partnerServiceId = partnerServiceId;
    }
}
