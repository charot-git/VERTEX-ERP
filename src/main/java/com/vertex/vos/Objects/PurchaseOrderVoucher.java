package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
public class PurchaseOrderVoucher {

    private int voucherId;
    private PurchaseOrder purchaseOrder;
    private int supplierId;
    private int coaId;
    private BigDecimal amount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String refNo;
    private BankAccount bankAccount;
    private int createdBy;
    private String status;
    private ChartOfAccounts coa;

    @Override
    public String toString() {
        return "PurchaseOrderVoucher{" +
                "voucherId=" + voucherId +
                ", purchaseOrder=" + purchaseOrder +
                ", supplier=" + supplierId +
                ", coaId=" + coaId +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", refNo='" + refNo + '\'' +
                ", bankAccount=" + bankAccount +
                ", createdBy=" + createdBy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PurchaseOrderVoucher that = (PurchaseOrderVoucher) o;

        return voucherId == that.voucherId;
    }

    @Override
    public int hashCode() {
        return voucherId;
    }
}
