package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
public class Company {
    // Getter methods for accessing the fields
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

}
