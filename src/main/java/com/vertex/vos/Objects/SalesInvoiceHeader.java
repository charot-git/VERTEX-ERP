package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceHeader {
    private int invoiceId;
    private String orderId;
    private String customerCode;
    private int salesmanId;
    private Date invoiceDate;
    private Date dueDate;
    private int paymentTerms;
    private String transactionStatus;
    private String paymentStatus;
    private double totalAmount;
    private int salesType;
    private String invoiceNo;
    private char priceType;
    private double vatAmount;
    private double discountAmount;
    private double netAmount;
    private int createdBy;
    private Timestamp createdDate;
    private int modifiedBy;
    private Timestamp modifiedDate;
    private int postedBy;
    private Timestamp postedDate;
    private int isReceipt;
    private int type;
    private String remarks;
    private String customerName;
    private String storeName;
    private Salesman salesman;
    private Customer customer;

}
