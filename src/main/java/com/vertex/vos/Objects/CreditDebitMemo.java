package com.vertex.vos.Objects;

import java.sql.Date;
import java.sql.Timestamp;

public class CreditDebitMemo {
    private int id;
    private String memoNumber;
    private int type;
    private String typeName;
    private int targetId; // supplier/customer
    private String targetName;
    private String orderNo;
    private Date date;
    private double amount;
    private String reason;
    private String status;
    private int chartOfAccount;
    private String chartOfAccountName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CreditDebitMemo() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemoNumber() {
        return memoNumber;
    }

    public void setMemoNumber(String memoNumber) {
        this.memoNumber = memoNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getChartOfAccount() {
        return chartOfAccount;
    }

    public void setChartOfAccount(int chartOfAccount) {
        this.chartOfAccount = chartOfAccount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
