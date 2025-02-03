package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class ChartOfAccounts {
    private int coaId;
    private String glCode;
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
    private boolean isPayment;

    public ChartOfAccounts() {

    }

    public ChartOfAccounts(int coaId, String glCode, String accountTitle, int bsisCodeId, String bsisCodeString, int accountTypeId, String accountTypeString, int balanceTypeId, String balanceTypeString, String description, boolean memoType, int addedBy, Timestamp dateAdded) {
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
