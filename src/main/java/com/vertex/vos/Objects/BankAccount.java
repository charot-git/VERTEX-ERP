package com.vertex.vos.Objects;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class BankAccount {
    private int bankId;
    private String bankName;
    private String accountNumber;
    private String bankDescription;

    public String getBankDescription() {
        return bankDescription;
    }

    public void setBankDescription(String bankDescription) {
        this.bankDescription = bankDescription;
    }

    private String branch;
    private String ifscCode;
    private BigDecimal openingBalance;
    private String province;
    private String city;
    private String baranggay;
    private String email;
    private String mobileNo;
    private String contactPerson;
    private boolean isActive;
    private Timestamp createdAt;
    private int createdBy;

    public BankAccount(){

    }

    public BankAccount(int bankId, String bankName, String accountNumber, String accountDescription, String branch, String ifscCode, BigDecimal openingBalance, String province, String city, String baranggay, String email, String mobileNo, String contactPerson, boolean isActive, Timestamp createdAt, int createdBy) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.bankDescription = accountDescription;
        this.branch = branch;
        this.ifscCode = ifscCode;
        this.openingBalance = openingBalance;
        this.province = province;
        this.city = city;
        this.baranggay = baranggay;
        this.email = email;
        this.mobileNo = mobileNo;
        this.contactPerson = contactPerson;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBaranggay() {
        return baranggay;
    }

    public void setBaranggay(String baranggay) {
        this.baranggay = baranggay;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
