package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class PurchaseOrderPayment {

    private PurchaseOrder purchaseOrder;
    private int supplierId;
    private String supplierName;
    private Double paidAmount;
    private int chartOfAccountId;
    private String chartOfAccountName;
    private Timestamp createdAt;
}
