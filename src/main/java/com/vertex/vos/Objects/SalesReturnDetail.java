package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class SalesReturnDetail {
    private int salesReturnDetailId;
    private SalesReturn salesReturn;
    private int productId;
    private Product product;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private double grossAmount;
    private double discountAmount;
    private String reason;
    private SalesReturnType salesReturnType;
    private DiscountType discountType;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
}
