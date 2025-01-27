package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDetail {

    int salesInvoiceDetailId;
    String orderId;
    SalesInvoiceHeader salesInvoiceNo;
    Product product;
    DiscountType discountType;
    int quantity;
    int availableQuantity;
    double unitPrice;
    double totalPrice;
    double grossAmount;
    Timestamp createdAt;
    Timestamp modifiedAt;
    double discountAmount;
}
