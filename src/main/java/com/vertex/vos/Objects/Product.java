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
    private String supplierName;

    public Product() {

    }
}