package com.vertex.vos.Constructors;

import java.sql.Date;

public class Company {
    private final int companyId;
    private final String companyName;
    private final String companyType;
    private final String companyCode;
    private final String companyFirstAddress;
    private final String companySecondAddress;
    private final String companyRegistrationNumber;
    private final String companyTIN;
    private final Date companyDateAdmitted;
    private final String companyContact;
    private final String companyEmail;
    private final String companyDepartment;
    private final byte[] companyLogo;
    private final String companyTags;

    public Company(int companyId, String companyName, String companyType, String companyCode,
                   String companyFirstAddress, String companySecondAddress, String companyRegistrationNumber,
                   String companyTIN, Date companyDateAdmitted, String companyContact, String companyEmail,
                   String companyDepartment, byte[] companyLogo, String companyTags) {
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

    public byte[] getCompanyLogo() {
        return companyLogo;
    }

    public String getCompanyTags() {
        return companyTags;
    }
}
