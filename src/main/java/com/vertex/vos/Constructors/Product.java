package com.vertex.vos.Constructors;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

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
    private int productNature;
    private int productSection;
    private int productShelfLife;
    private double productWeight;
    private int maintainingQuantity;
    private double quantity;
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
    private String productNatureString;
    private String productSectionString;
    private String unitOfMeasurementString;

    public String getProductBrandString() {
        return productBrandString;
    }

    public void setProductBrandString(String productBrandString) {
        this.productBrandString = productBrandString;
    }

    public String getProductCategoryString() {
        return productCategoryString;
    }

    public void setProductCategoryString(String productCategoryString) {
        this.productCategoryString = productCategoryString;
    }

    public String getProductClassString() {
        return productClassString;
    }

    public void setProductClassString(String productClassString) {
        this.productClassString = productClassString;
    }

    public String getProductSegmentString() {
        return productSegmentString;
    }

    public void setProductSegmentString(String productSegmentString) {
        this.productSegmentString = productSegmentString;
    }

    public String getProductNatureString() {
        return productNatureString;
    }

    public void setProductNatureString(String productNatureString) {
        this.productNatureString = productNatureString;
    }

    public String getProductSectionString() {
        return productSectionString;
    }

    public void setProductSectionString(String productSectionString) {
        this.productSectionString = productSectionString;
    }

    public String getUnitOfMeasurementString() {
        return unitOfMeasurementString;
    }

    public void setUnitOfMeasurementString(String unitOfMeasurementString) {
        this.unitOfMeasurementString = unitOfMeasurementString;
    }

    public Product(int productId, int isActive, int parentId, String productName, String barcode, String productCode, String productImage, String description, String shortDescription, Date dateAdded, Timestamp lastUpdated, int productBrand, int productCategory, int productClass, int productSegment, int productNature, int productSection, int productShelfLife, double productWeight, int maintainingQuantity, double quantity, int unitOfMeasurement, int unitOfMeasurementCount, double estimatedUnitCost, double estimatedExtendedCost, double pricePerUnit, double costPerUnit, double priceA, double priceB, double priceC, double priceD, double priceE, String productBrandString, String productCategoryString, String productClassString, String productSegmentString, String productNatureString, String productSectionString, String unitOfMeasurementString) {
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
        this.productNature = productNature;
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
        this.productNatureString = productNatureString;
        this.productSectionString = productSectionString;
        this.unitOfMeasurementString = unitOfMeasurementString;
    }

    public Product() {
        
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
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

    public int getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(int productBrand) {
        this.productBrand = productBrand;
    }

    public int getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(int productCategory) {
        this.productCategory = productCategory;
    }

    public int getProductClass() {
        return productClass;
    }

    public void setProductClass(int productClass) {
        this.productClass = productClass;
    }

    public int getProductSegment() {
        return productSegment;
    }

    public void setProductSegment(int productSegment) {
        this.productSegment = productSegment;
    }

    public int getProductNature() {
        return productNature;
    }

    public void setProductNature(int productNature) {
        this.productNature = productNature;
    }

    public int getProductSection() {
        return productSection;
    }

    public void setProductSection(int productSection) {
        this.productSection = productSection;
    }

    public int getProductShelfLife() {
        return productShelfLife;
    }

    public void setProductShelfLife(int productShelfLife) {
        this.productShelfLife = productShelfLife;
    }

    public double getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(double productWeight) {
        this.productWeight = productWeight;
    }

    public int getMaintainingQuantity() {
        return maintainingQuantity;
    }

    public void setMaintainingQuantity(int maintainingQuantity) {
        this.maintainingQuantity = maintainingQuantity;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(int unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public int getUnitOfMeasurementCount() {
        return unitOfMeasurementCount;
    }

    public void setUnitOfMeasurementCount(int unitOfMeasurementCount) {
        this.unitOfMeasurementCount = unitOfMeasurementCount;
    }

    public double getEstimatedUnitCost() {
        return estimatedUnitCost;
    }

    public void setEstimatedUnitCost(double estimatedUnitCost) {
        this.estimatedUnitCost = estimatedUnitCost;
    }

    public double getEstimatedExtendedCost() {
        return estimatedExtendedCost;
    }

    public void setEstimatedExtendedCost(double estimatedExtendedCost) {
        this.estimatedExtendedCost = estimatedExtendedCost;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
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

    public double getPriceD() {
        return priceD;
    }

    public void setPriceD(double priceD) {
        this.priceD = priceD;
    }

    public double getPriceE() {
        return priceE;
    }

    public void setPriceE(double priceE) {
        this.priceE = priceE;
    }
}