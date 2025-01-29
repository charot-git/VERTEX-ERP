package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
public class CreditDebitMemo {
    private int id;
    private String memoNumber;
    private int type;
    private String typeName;
    private int targetId; // supplier/customer
    private String targetName;
    private Date date;
    private double amount;
    private String reason;
    private String status;
    private int chartOfAccount;
    private String chartOfAccountName;
    private Timestamp createdAt;
    private boolean isPending;

    private Timestamp updatedAt;
    private int encoderId;

    public CreditDebitMemo() {

    }

}
