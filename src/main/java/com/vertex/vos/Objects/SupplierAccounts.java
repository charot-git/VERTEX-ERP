package com.vertex.vos.Objects;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SupplierAccounts {
    private int supplierId;
    private String supplierName; // Assuming a name field for better readability
    private String documentType; // Can be "Purchase Order Payment" or "Supplier Memo"
    private String documentNumber;
    private BigDecimal amount;
    private Integer chartOfAccountId; // Use Integer for nullable fields
    private String chartOfAccountName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int transactionTypeId;
    private String transactionTypeName; // New field for transaction type

    // New fields for Credit and Debit amounts
    private BigDecimal creditAmount = BigDecimal.ZERO;
    private BigDecimal debitAmount = BigDecimal.ZERO;

    // Constructors
    public SupplierAccounts() {
    }

    public SupplierAccounts(int supplierId, String supplierName, String documentType, String documentNumber,
                            BigDecimal amount, Integer chartOfAccountId, String chartOfAccountName,
                            Timestamp createdAt, Timestamp updatedAt, int transactionTypeId, String transactionTypeName) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.amount = amount;
        this.chartOfAccountId = chartOfAccountId;
        this.chartOfAccountName = chartOfAccountName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.transactionTypeId = transactionTypeId;
        this.transactionTypeName = transactionTypeName;
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getChartOfAccountId() {
        return chartOfAccountId;
    }

    public void setChartOfAccountId(Integer chartOfAccountId) {
        this.chartOfAccountId = chartOfAccountId;
    }

    public String getChartOfAccountName() {
        return chartOfAccountName;
    }

    public void setChartOfAccountName(String chartOfAccountName) {
        this.chartOfAccountName = chartOfAccountName;
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

    public int getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(int transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    @Override
    public String toString() {
        return "SupplierAccounts{" +
                "supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", documentType='" + documentType + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", amount=" + amount +
                ", chartOfAccountId=" + chartOfAccountId +
                ", chartOfAccountName='" + chartOfAccountName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", transactionTypeId=" + transactionTypeId +
                ", transactionTypeName='" + transactionTypeName + '\'' +
                ", creditAmount=" + creditAmount +
                ", debitAmount=" + debitAmount +
                '}';
    }
}
