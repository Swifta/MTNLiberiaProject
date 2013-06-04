/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swifta.schoolportal.entities;

/**
 *
 * @author Opeyemi
 */
public class Student {

    private String name = "";
    private String identNo = "";
    private String lastUpdate = "";
    private int id = 0;
    private String schoolCode = "";

    public String getSchoolDetails() {
        return schoolDetails;
    }

    public void setSchoolDetails(String schoolDetails) {
        this.schoolDetails = schoolDetails;
    }
    private String schoolDetails = "";

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentNo() {
        return identNo;
    }

    public void setIdentNo(String identNo) {
        this.identNo = identNo;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name + " " + this.identNo + " " + this.lastUpdate;

    }
}
