package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceHeader {
    private int invoiceId;                  // `invoice_id`
    private String orderId;                 // `order_id`
    private String customerCode;            // `customer_code`
    private int salesmanId;                 // `salesman_id`
    private Timestamp invoiceDate;          // `invoice_date`
    private Timestamp dispatchDate;         // `dispatch_date`
    private Timestamp dueDate;              // `due_date`
    private int paymentTerms;               // `payment_terms`
    private String transactionStatus;       // `transaction_status`
    private String paymentStatus;           // `payment_status`
    private double totalAmount;             // `total_amount`
    private int salesType;                  // `sales_type`
    private SalesInvoiceType invoiceType;   // `invoice_type` (mapped to object)
    private String invoiceNo;               // `invoice_no`
    private String priceType;                 // `price_type`
    private double vatAmount;               // `vat_amount`
    private double discountAmount;          // `discount_amount`
    private double netAmount;               // `net_amount`
    private int createdBy;                  // `created_by`
    private Timestamp createdDate;          // `created_date`
    private int modifiedBy;                 // `modified_by`
    private Timestamp modifiedDate;         // `modified_date`
    private int postedBy;                   // `posted_by`
    private Timestamp postedDate;           // `posted_date`
    private String remarks;                 // `remarks`
    private boolean isReceipt;              // `isReceipt` (converted from BIT(1))
    private boolean isPosted;               // `isPosted`
    private boolean isDispatched;           // `isDispatched`
    private double grossAmount;            // `gross_amount`
    // Additional fields not in the database
    private String customerName;            // Additional field
    private String storeName;               // Additional field
    private Salesman salesman;              // Reference to a Salesman object
    private Customer customer;              // Reference to a Customer object

    private ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList();
    private ObservableList<SalesInvoicePayment> salesInvoicePayments = FXCollections.observableArrayList();

}
