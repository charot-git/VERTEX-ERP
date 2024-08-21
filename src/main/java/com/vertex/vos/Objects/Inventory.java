package com.vertex.vos.Objects;

import java.time.LocalDateTime;

public class Inventory {
    private int branchId;
    private String branchName;
    private int productId;
    private String productDescription;
    private int quantity;
    private LocalDateTime lastRestockDate;
    private int reservedQuantity; // Add this fieldprivate String brand; // Added field
    private String brand;
    private String category; // Added field
    private String productClass; // Added field
    private String productSegment; // Added field
    private String productSection; // Added field
    private String productNature; // Added field

    public String getProductNature() {
        return productNature;
    }

    public void setProductNature(String productNature) {
        this.productNature = productNature;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductClass() {
        return productClass;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public String getProductSegment() {
        return productSegment;
    }

    public void setProductSegment(String productSegment) {
        this.productSegment = productSegment;
    }

    public String getProductSection() {
        return productSection;
    }

    public void setProductSection(String productSection) {
        this.productSection = productSection;
    }

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

    public Inventory(int branchId, String branchName, int productId, String productDescription, int quantity, LocalDateTime lastRestockDate, String brand, String category, String productClass, String productSegment, String productSection, String productNature) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.productId = productId;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.lastRestockDate = lastRestockDate;
        this.brand = brand;
        this.category = category;
        this.productClass = productClass;
        this.productSegment = productSegment;
        this.productSection = productSection;
        this.productNature = productNature;
    }
}
