package com.vertex.vos.Objects;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesInvoicePayment {
    private int id;
    private SalesInvoiceHeader invoice;
    private String orderId;
    private ChartOfAccounts chartOfAccount;
    private BankName bank;
    private String referenceNo;
    private double paidAmount;
    private Timestamp datePaid;
    private Timestamp dateEncoded;
}
