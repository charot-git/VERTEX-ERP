package com.vertex.vos.Objects;

import javafx.beans.value.ObservableValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
public class Product {

    private int productId;
    private int isActive;
    private int parentId;
    private String productName;
    private String barcode;
    private String productCode;
    private String productImage;
    private String description;
    private String shortDescription;
    private Date dateAdded;
    private Timestamp lastUpdated;
    private int productBrand;
    private int productCategory;
    private int productClass;
    private int productSegment;
    private int productSection;
    private int productShelfLife;
    private double productWeight;
    private int maintainingQuantity;
    private int quantity;
    private int reservedQuantity;
    private int unitOfMeasurement;
    private int unitOfMeasurementCount;
    private double estimatedUnitCost;
    private double estimatedExtendedCost;
    private double pricePerUnit;
    private double costPerUnit;
    private double priceA;
    private double priceB;
    private double priceC;
    private double priceD;
    private double priceE;
    private String productBrandString;
    private String productCategoryString;
    private String productClassString;
    private String productSegmentString;
    private String productSectionString;
    private String unitOfMeasurementString;
    private DiscountType discountType;
    private Unit unit;
    private String discountTypeString;
    public Product(int productId, int isActive, int parentId, String productName, String barcode, String productCode, String productImage, String description, String shortDescription, Date dateAdded, Timestamp lastUpdated, int productBrand, int productCategory, int productClass, int productSegment, int productNature, int productSection, int productShelfLife, double productWeight, int maintainingQuantity, int quantity, int unitOfMeasurement, int unitOfMeasurementCount, double estimatedUnitCost, double estimatedExtendedCost, double pricePerUnit, double costPerUnit, double priceA, double priceB, double priceC, double priceD, double priceE, String productBrandString, String productCategoryString, String productClassString, String productSegmentString, String productNatureString, String productSectionString, String unitOfMeasurementString) {
        this.productId = productId;
        this.isActive = isActive;
        this.parentId = parentId;
        this.productName = productName;
        this.barcode = barcode;
        this.productCode = productCode;
        this.productImage = productImage;
        this.description = description;
        this.shortDescription = shortDescription;
        this.dateAdded = dateAdded;
        this.lastUpdated = lastUpdated;
        this.productBrand = productBrand;
        this.productCategory = productCategory;
        this.productClass = productClass;
        this.productSegment = productSegment;
        this.productSection = productSection;
        this.productShelfLife = productShelfLife;
        this.productWeight = productWeight;
        this.maintainingQuantity = maintainingQuantity;
        this.quantity = quantity;
        this.unitOfMeasurement = unitOfMeasurement;
        this.unitOfMeasurementCount = unitOfMeasurementCount;
        this.estimatedUnitCost = estimatedUnitCost;
        this.estimatedExtendedCost = estimatedExtendedCost;
        this.pricePerUnit = pricePerUnit;
        this.costPerUnit = costPerUnit;
        this.priceA = priceA;
        this.priceB = priceB;
        this.priceC = priceC;
        this.priceD = priceD;
        this.priceE = priceE;
        this.productBrandString = productBrandString;
        this.productCategoryString = productCategoryString;
        this.productClassString = productClassString;
        this.productSegmentString = productSegmentString;
        this.productSectionString = productSectionString;
        this.unitOfMeasurementString = unitOfMeasurementString;
    }

    public Product() {

    }
}