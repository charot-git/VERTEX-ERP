package com.vertex.vos.Objects;

public class ProductSEO {
    private String description;
    private String productBrand;
    private String productCategory;
    private String productClass;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String productSegment;
    private String productNature;
    private String productSection;

    public ProductSEO() {

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

    public String getProductNature() {
        return productNature;
    }

    public void setProductNature(String productNature) {
        this.productNature = productNature;
    }

    public String getProductSection() {
        return productSection;
    }

    public void setProductSection(String productSection) {
        this.productSection = productSection;
    }

    public ProductSEO(String productBrand, String productCategory, String productClass, String productSegment, String productNature, String productSection) {
        this.productBrand = productBrand;
        this.productCategory = productCategory;
        this.productClass = productClass;
        this.productSegment = productSegment;
        this.productNature = productNature;
        this.productSection = productSection;
    }
}
