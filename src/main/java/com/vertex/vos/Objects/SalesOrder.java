package com.vertex.vos.Objects;

import com.vertex.vos.Enums.SalesOrderStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    private int orderId;
    private String orderNo;
    private String purchaseNo;
    private Customer customer;
    private Supplier supplier;
    private Salesman salesman;
    private Branch branch;
    private Date orderDate;
    private Timestamp deliveryDate;
    private Timestamp dueDate;
    private Integer paymentTerms;
    private SalesOrderStatus orderStatus;
    private Double totalAmount;
    private Operation salesType;
    private Double discountAmount;
    private SalesInvoiceType invoiceType;
    private Double netAmount;
    private User createdBy;
    private Timestamp createdDate;
    private User modifiedBy;
    private Timestamp modifiedDate;
    private User postedBy;
    private Timestamp postedDate;
    private String remarks;
    private Boolean isDelivered;
    private Boolean isCancelled;

    private Timestamp forApprovalAt;
    private Timestamp forConsolidationAt;
    private Timestamp forPickingAt;
    private Timestamp forInvoicingAt;
    private Timestamp forLoadingAt;
    private Timestamp forShippingAt;
    private Timestamp deliveredAt;
    private Timestamp onHoldAt;
    private Timestamp cancelledAt;

    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();
}
