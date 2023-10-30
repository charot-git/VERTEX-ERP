package com.vertex.vos.Constructors;

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
    private String priceA;
    private String priceC;
    private String priceB;
    private String productBrand;
    private String productCategory;

    public Product() {
        
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Product(int productId, String productName, String productCode, double costPerUnit, double pricePerUnit, double productDiscount, int quantityAvailable, String description, String supplierName, Date dateAdded, Timestamp lastUpdated, double priceA, double priceB, double priceC, String productBrand, String productCategory, String unitOfMeasurement) {
        this.dateAdded = dateAdded;
    }

    private Date dateAdded;
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

    public String getPriceA() {
        return priceA;
    }

    public void setPriceA(String priceA) {
        this.priceA = priceA;
    }

    public String getPriceC() {
        return priceC;
    }

    public void setPriceC(String priceC) {
        this.priceC = priceC;
    }

    public String getPriceB() {
        return priceB;
    }

    public void setPriceB(String priceB) {
        this.priceB = priceB;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductUnitOfMeasurement() {
        return productUnitOfMeasurement;
    }

    public void setProductUnitOfMeasurement(String productUnitOfMeasurement) {
        this.productUnitOfMeasurement = productUnitOfMeasurement;
    }

    public Product(int productId, String productName, String productCode, double costPerUnit, double pricePerUnit, double productDiscount, int quantityAvailable, String description, String supplierName, String priceA, String priceC, String priceD, String productBrand, String productCategory, String productUnitOfMeasurement) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.costPerUnit = costPerUnit;
        this.pricePerUnit = pricePerUnit;
        this.productDiscount = productDiscount;
        this.quantityAvailable = quantityAvailable;
        this.description = description;
        this.supplierName = supplierName;
        this.priceA = priceA;
        this.priceC = priceC;
        this.priceB = priceD;
        this.productBrand = productBrand;
        this.productCategory = productCategory;
        this.productUnitOfMeasurement = productUnitOfMeasurement;
    }

    private String productUnitOfMeasurement;


}