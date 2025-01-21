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
    String salesInvoiceNo;
    Product product;
    int quantity;
    double unitPrice;
    double totalPrice;
    Timestamp createdAt;
    Timestamp modifiedAt;

}
