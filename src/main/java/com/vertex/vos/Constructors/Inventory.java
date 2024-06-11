package com.vertex.vos.Constructors;

import java.time.LocalDateTime;

public class Inventory {
    private int branchId;
    private String branchName;
    private int productId;
    private String productDescription;
    private int quantity;
    private LocalDateTime lastRestockDate;
    private int reservedQuantity; // Add this field

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Inventory() {
    }

    public Inventory(int quantity, LocalDateTime lastRestockDate) {
        this.quantity = quantity;
        this.lastRestockDate = lastRestockDate;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastRestockDate() {
        return lastRestockDate;
    }

    public void setLastRestockDate(LocalDateTime lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }

    public void addQuantity(int quantityToAdd) {
        this.quantity += quantityToAdd;
    }

    public Inventory(int branchId, String branchName, int productId, String productDescription, int quantity, LocalDateTime lastRestockDate) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.productId = productId;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.lastRestockDate = lastRestockDate;
    }
}
