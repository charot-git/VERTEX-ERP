package com.vertex.vos;

import java.sql.Date;
import java.sql.Timestamp;

public class Product {
    private int productId;
    private String productName;
    private String productCode;
    private double costPerUnit;
    private double pricePerUnit;
    private double productDiscount;
    private int quantityAvailable;
    private String description;
    private String supplierName;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(double productDiscount) {
        this.productDiscount = productDiscount;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getPriceA() {
        return priceA;
    }

    public void setPriceA(double priceA) {
        this.priceA = priceA;
    }

    public double getPriceB() {
        return priceB;
    }

    public void setPriceB(double priceB) {
        this.priceB = priceB;
    }

    public double getPriceC() {
        return priceC;
    }

    public void setPriceC(double priceC) {
        this.priceC = priceC;
    }

    public Product(int productId, String productName, String productCode, double costPerUnit, double pricePerUnit, double productDiscount, int quantityAvailable, String description, String supplierName, Date dateAdded, Timestamp lastUpdated, double priceA, double priceB, double priceC) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.costPerUnit = costPerUnit;
        this.pricePerUnit = pricePerUnit;
        this.productDiscount = productDiscount;
        this.quantityAvailable = quantityAvailable;
        this.description = description;
        this.supplierName = supplierName;
        this.dateAdded = dateAdded;
        this.lastUpdated = lastUpdated;
        this.priceA = priceA;
        this.priceB = priceB;
        this.priceC = priceC;
    }

    private Date dateAdded;
    private Timestamp lastUpdated;
    private double priceA;
    private double priceB;
    private double priceC;

    // Constructors, getters, and setters can be generated for the class properties
}
