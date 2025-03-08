package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    private int orderId;
    private String orderNo;
    private Customer customer;
    private Supplier supplier;
    private Salesman salesman;
    private Branch branch;
    private Timestamp orderDate;
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

    @Getter
    public enum SalesOrderStatus {
        FOR_APPROVAL("For Approval"),
        PENDING("Pending"),
        PICKED("Picked"),
        INVOICED("Invoiced"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled"),
        ON_HOLD("On Hold");

        private final String dbValue;

        SalesOrderStatus(String dbValue) {
            this.dbValue = dbValue;
        }

        // Convert from DB value to Enum safely
        public static SalesOrderStatus fromDbValue(String dbValue) {
            for (SalesOrderStatus status : SalesOrderStatus.values()) {
                if (status.dbValue.equalsIgnoreCase(dbValue)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown order status: " + dbValue);
        }
    }


    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();

}
