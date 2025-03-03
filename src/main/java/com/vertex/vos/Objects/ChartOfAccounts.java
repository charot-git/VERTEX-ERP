package com.vertex.vos.Objects;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@ToString
@Data
public class ChartOfAccounts {
    private int coaId;
    private String glCode;
    private String accountTitle;
    private int bsisCodeId;
    private String bsisCodeString;
    private int accountTypeId;
    private String accountTypeString;
    private BalanceType balanceType;
    private String description;
    private boolean memoType;
    private int addedBy;
    private Timestamp dateAdded;
    private boolean isPayment;

}
