package com.vertex.vos.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDetails {

    private int detailId;
    private Product product; // Assuming a Product class that represents the product entity
    private SalesOrder salesOrder; // Represents the sales order to which the detail belongs
    private double unitPrice;
    private int orderedQuantity;
    private int servedQuantity;
    private int allocatedQuantity;
    private DiscountType discountType;
    private double discountAmount;
    private double grossAmount;
    private double netAmount;
    private double allocatedAmount;
    private String remarks;
    private Timestamp createdDate;
    private Timestamp modifiedDate;

}
