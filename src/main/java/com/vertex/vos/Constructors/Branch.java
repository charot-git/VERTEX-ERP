package com.vertex.vos.Constructors;

import java.sql.Date;

public class Branch {
    private int id;
    private String branchDescription;
    private String branchName;
    private int branchHeadId;
    private String branchHeadName;
    private String branchCode;
    private String stateProvince;
    private String city;
    private String brgy;
    private String phoneNumber;
    private String postalCode;
    private Date dateAdded;

    public Branch(int id, String branchDescription, String branchName, String branchHeadName,
                  String branchCode, String stateProvince, String city, String brgy,
                  String phoneNumber, String postalCode, Date dateAdded) {
        this.id = id;
        this.branchDescription = branchDescription;
        this.branchName = branchName;
        this.branchHeadId = branchHeadId;
        this.branchHeadName = branchHeadName;
        this.branchCode = branchCode;
        this.stateProvince = stateProvince;
        this.city = city;
        this.brgy = brgy;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.dateAdded = dateAdded;
    }

    public Branch() {

    }

    public Branch(int id, String branchDescription, String branchName, int branchHead, String branchCode, String stateProvince, String city, String brgy, String phoneNumber, String postalCode, Date dateAdded) {
        this.id = id;
        this.branchDescription = branchDescription;
        this.branchName = branchName;
        this.branchHeadId = branchHead;
        this.branchCode = branchCode;
        this.stateProvince = stateProvince;
        this.city = city;
        this.brgy = brgy;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.dateAdded = dateAdded;
    }

    // Constructors, getters, and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranchDescription() {
        return branchDescription;
    }

    public void setBranchDescription(String branchDescription) {
        this.branchDescription = branchDescription;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getBranchHeadId() {
        return branchHeadId;
    }

    public void setBranchHeadId(int branchHeadId) {
        this.branchHeadId = branchHeadId;
    }

    public String getBranchHeadName() {
        return branchHeadName;
    }

    public void setBranchHeadName(String branchHeadName) {
        this.branchHeadName = branchHeadName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBrgy() {
        return brgy;
    }

    public void setBrgy(String brgy) {
        this.brgy = brgy;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
}
