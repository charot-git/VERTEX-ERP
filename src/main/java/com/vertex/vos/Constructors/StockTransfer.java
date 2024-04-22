package com.vertex.vos.Constructors;

public class StockTransfer {
    private String orderNo;
    private int sourceBranch;
    private int targetBranch;
    private int productId;
    private int orderedQuantity;
    private double amount;
    private java.sql.Date dateRequested;
    private java.sql.Date leadDate;
    private String status;

    // Constructor
    public StockTransfer(String orderNo, int sourceBranch, int targetBranch, int productId, int orderedQuantity, double amount, java.sql.Date dateRequested, java.sql.Date leadDate, String status) {
        this.orderNo = orderNo;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.productId = productId;
        this.orderedQuantity = orderedQuantity;
        this.amount = amount;
        this.dateRequested = dateRequested;
        this.leadDate = leadDate;
        this.status = status;
    }

    public StockTransfer() {
    }

    // Getters and setters
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(int sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public int getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(int targetBranch) {
        this.targetBranch = targetBranch;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public java.sql.Date getDateRequested() {
        return dateRequested;
    }

    public void setDateRequested(java.sql.Date dateRequested) {
        this.dateRequested = dateRequested;
    }

    public java.sql.Date getLeadDate() {
        return leadDate;
    }

    public void setLeadDate(java.sql.Date leadDate) {
        this.leadDate = leadDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
