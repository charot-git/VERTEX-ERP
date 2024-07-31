package com.vertex.vos.Objects;

import java.sql.Timestamp;

public class ChartOfAccounts {
    private int coaId;
    private int glCode;
    private String accountTitle;
    private int bsisCodeId;
    private String bsisCodeString;
    private int accountTypeId;
    private String accountTypeString;
    private int balanceTypeId;
    private String balanceTypeString;
    private String description;
    private boolean memoType;
    private int addedBy;
    private Timestamp dateAdded;

    public int getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(int addedBy) {
        this.addedBy = addedBy;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }

    public ChartOfAccounts() {

    }

    public boolean isMemoType() {
        return memoType;
    }

    public void setMemoType(boolean memoType) {
        this.memoType = memoType;
    }

    public int getCoaId() {
        return coaId;
    }

    public void setCoaId(int coaId) {
        this.coaId = coaId;
    }

    public int getGlCode() {
        return glCode;
    }

    public void setGlCode(int glCode) {
        this.glCode = glCode;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public int getBsisCodeId() {
        return bsisCodeId;
    }

    public void setBsisCodeId(int bsisCodeId) {
        this.bsisCodeId = bsisCodeId;
    }

    public String getBsisCodeString() {
        return bsisCodeString;
    }

    public void setBsisCodeString(String bsisCodeString) {
        this.bsisCodeString = bsisCodeString;
    }

    public int getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(int accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getAccountTypeString() {
        return accountTypeString;
    }

    public void setAccountTypeString(String accountTypeString) {
        this.accountTypeString = accountTypeString;
    }

    public int getBalanceTypeId() {
        return balanceTypeId;
    }

    public void setBalanceTypeId(int balanceTypeId) {
        this.balanceTypeId = balanceTypeId;
    }

    public String getBalanceTypeString() {
        return balanceTypeString;
    }

    public void setBalanceTypeString(String balanceTypeString) {
        this.balanceTypeString = balanceTypeString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChartOfAccounts(int coaId, int glCode, String accountTitle, int bsisCodeId, String bsisCodeString, int accountTypeId, String accountTypeString, int balanceTypeId, String balanceTypeString, String description, boolean memoType, int addedBy, Timestamp dateAdded) {
        this.coaId = coaId;
        this.glCode = glCode;
        this.accountTitle = accountTitle;
        this.bsisCodeId = bsisCodeId;
        this.bsisCodeString = bsisCodeString;
        this.accountTypeId = accountTypeId;
        this.accountTypeString = accountTypeString;
        this.balanceTypeId = balanceTypeId;
        this.balanceTypeString = balanceTypeString;
        this.description = description;
        this.memoType = memoType;
        this.addedBy = addedBy;
        this.dateAdded = dateAdded;
    }
}
