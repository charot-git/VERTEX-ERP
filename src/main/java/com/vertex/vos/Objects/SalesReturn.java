package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
public class SalesReturn {
    private int returnId;
    private String returnNumber;
    private String customerCode;
    private Customer customer;
    private Timestamp returnDate;
    private double totalAmount;  // Changed to BigDecimal for currency
    private String remarks;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
    private boolean isThirdParty;
    private String priceType;

    public SalesReturn() {
    }

    public SalesReturn(int returnId, String returnNumber, String customerId, Customer customer, Timestamp returnDate, double totalAmount, String remarks, int createdBy, Timestamp createdAt, Timestamp updatedAt, String status, boolean isThirdParty) {
        this.returnId = returnId;
        this.returnNumber = returnNumber;
        this.customerCode = customerId;
        this.customer = customer;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.remarks = remarks;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.isThirdParty = isThirdParty;
    }

    public SalesReturn(int returnId, String returnNumber, String customerId, Timestamp returnDate, double totalAmount, String remarks, int createdBy, Timestamp createdAt, Timestamp updatedAt, String status, boolean isThirdParty) {
        this.returnId = returnId;
        this.returnNumber = returnNumber;
        this.customerCode = customerId;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.remarks = remarks;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.isThirdParty = isThirdParty;
    }
    @Override
    public String toString() {
        return "SalesReturn{" +
                "returnId=" + returnId +
                ", returnNumber='" + returnNumber + '\'' +
                ", customerId=" + customerCode +
                ", returnDate=" + returnDate +
                ", totalAmount=" + totalAmount +
                ", remarks='" + remarks + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
