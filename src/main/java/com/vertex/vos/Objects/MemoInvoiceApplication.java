package com.vertex.vos.Objects;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MemoInvoiceApplication {
    private CustomerMemo customerMemo;
    private SalesInvoiceHeader salesInvoiceHeader;
    private double amount;
    private Timestamp dateApplied;
}
