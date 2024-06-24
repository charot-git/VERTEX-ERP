package com.vertex.vos.Constructors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentsToSupplier {
    private int paymentId;
    private int supplierId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private int paymentMethodId;
    private String referenceNumber;
    private int chartOfAccountId;
    private int transactionTypeId;
    private String notes;
    private LocalDateTime createdAt;
    private int createdBy;

    // Constructors
    public PaymentsToSupplier() {
        // Default constructor
    }

    public PaymentsToSupplier(int supplierId, LocalDate paymentDate, BigDecimal amount, int paymentMethodId,
                              int chartOfAccountId, int transactionTypeId, String notes, int createdBy) {
        this.supplierId = supplierId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
        this.chartOfAccountId = chartOfAccountId;
        this.transactionTypeId = transactionTypeId;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    // Getters and setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public int getChartOfAccountId() {
        return chartOfAccountId;
    }

    public void setChartOfAccountId(int chartOfAccountId) {
        this.chartOfAccountId = chartOfAccountId;
    }

    public int getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(int transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // Optional: Override toString() for debugging purposes
    @Override
    public String toString() {
        return "PaymentsToSupplier{" +
                "paymentId=" + paymentId +
                ", supplierId=" + supplierId +
                ", paymentDate=" + paymentDate +
                ", amount=" + amount +
                ", paymentMethodId=" + paymentMethodId +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", chartOfAccountId=" + chartOfAccountId +
                ", transactionTypeId=" + transactionTypeId +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
