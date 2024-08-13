package com.vertex.vos.Objects;

import java.sql.Timestamp;

public class SalesReturn {
    private int returnId;
    private String returnNumber;
    private int customerId;
    private Timestamp returnDate;
    private double totalAmount;
    private String remarks;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public SalesReturn() {

    }
    public SalesReturn(int returnId, String returnNumber, int customerId, Timestamp returnDate, double totalAmount, String remarks, int createdBy, Timestamp createdAt, Timestamp updatedAt) {
        this.returnId = returnId;
        this.returnNumber = returnNumber;
        this.customerId = customerId;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.remarks = remarks;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returnDate) {
        this.returnDate = returnDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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