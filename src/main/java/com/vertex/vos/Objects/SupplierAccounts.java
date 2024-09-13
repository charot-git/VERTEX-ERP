package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
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
    private BigDecimal creditAmount = BigDecimal.ZERO;
    private BigDecimal debitAmount = BigDecimal.ZERO;

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
