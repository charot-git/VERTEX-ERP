package com.vertex.vos.Objects;

import java.sql.Timestamp;

public class SalesReturnDetails {
    private int detailId;
    private int returnId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String reason;
    private int salesReturnTypeId;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public SalesReturnDetails() {
    }

    public SalesReturnDetails(int detailId, int returnId, int productId, int quantity, double unitPrice, double totalPrice, String reason, int salesReturnTypeId, int createdBy, Timestamp createdAt, Timestamp updatedAt) {
        this.detailId = detailId;
        this.returnId = returnId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.reason = reason;
        this.salesReturnTypeId = salesReturnTypeId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getSalesReturnTypeId() {
        return salesReturnTypeId;
    }

    public void setSalesReturnTypeId(int salesReturnTypeId) {
        this.salesReturnTypeId = salesReturnTypeId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
