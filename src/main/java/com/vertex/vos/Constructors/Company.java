package com.vertex.vos.Constructors;

import java.sql.Date;

public class Company {
    private int companyId;
    private String companyName;
    private String companyType;
    private String companyCode;
    private String companyFirstAddress;
    private String companySecondAddress;
    private String companyRegistrationNumber;
    private String companyTIN;
    private Date companyDateAdmitted;
    private String companyContact;
    private String companyEmail;
    private String companyDepartment;
    private String companyLogo;
    private String companyTags;

    public Company(int companyId, String companyName, String companyType, String companyCode,
                   String companyFirstAddress, String companySecondAddress, String companyRegistrationNumber,
                   String companyTIN, Date companyDateAdmitted, String companyContact, String companyEmail,
                   String companyDepartment, String companyLogo, String companyTags) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyType = companyType;
        this.companyCode = companyCode;
        this.companyFirstAddress = companyFirstAddress;
        this.companySecondAddress = companySecondAddress;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.companyTIN = companyTIN;
        this.companyDateAdmitted = companyDateAdmitted;
        this.companyContact = companyContact;
        this.companyEmail = companyEmail;
        this.companyDepartment = companyDepartment;
        this.companyLogo = companyLogo;
        this.companyTags = companyTags;
    }

    public Company() {

    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public void setCompanyFirstAddress(String companyFirstAddress) {
        this.companyFirstAddress = companyFirstAddress;
    }

    public void setCompanySecondAddress(String companySecondAddress) {
        this.companySecondAddress = companySecondAddress;
    }

    public void setCompanyRegistrationNumber(String companyRegistrationNumber) {
        this.companyRegistrationNumber = companyRegistrationNumber;
    }

    public void setCompanyTIN(String companyTIN) {
        this.companyTIN = companyTIN;
    }

    public void setCompanyDateAdmitted(Date companyDateAdmitted) {
        this.companyDateAdmitted = companyDateAdmitted;
    }

    public void setCompanyContact(String companyContact) {
        this.companyContact = companyContact;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public void setCompanyDepartment(String companyDepartment) {
        this.companyDepartment = companyDepartment;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public void setCompanyTags(String companyTags) {
        this.companyTags = companyTags;
    }

    // Getter methods for accessing the fields
    public int getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getCompanyFirstAddress() {
        return companyFirstAddress;
    }

    public String getCompanySecondAddress() {
        return companySecondAddress;
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public String getCompanyTIN() {
        return companyTIN;
    }

    public Date getCompanyDateAdmitted() {
        return companyDateAdmitted;
    }

    public String getCompanyContact() {
        return companyContact;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getCompanyDepartment() {
        return companyDepartment;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public String getCompanyTags() {
        return companyTags;
    }
}
