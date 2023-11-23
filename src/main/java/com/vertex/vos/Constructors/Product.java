package com.vertex.vos.Constructors;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Product {
    private int product_id;
    private String product_name;
    private String product_code;
    private String description;
    private int supplier_name;
    private Date date_added;
    private Timestamp last_updated;
    private int product_brand;
    private int product_category;
    private int product_segment;
    private int product_section;
    private Boolean isActive;
    private int product_class;
    private int base_unit;
    private String product_image;
    private int product_nature;
    private int product_shelf_life;
    private int maintaining_base_quantity;
    private Double product_base_weight;

    private String supplierFromId;

    private String brandFromId;

    private String categoryFromId;

    private String segmentFromId;

    private String sectionFromId;

    public String getBaseUnitFromId() {
        return baseUnitFromId;
    }

    public void setBaseUnitFromId(String baseUnitFromId) {
        this.baseUnitFromId = baseUnitFromId;
    }

    public Product(String baseUnitFromId) {
        this.baseUnitFromId = baseUnitFromId;
    }

    private String baseUnitFromId;

    public Product() {

    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSupplier_name() {
        return supplier_name;
    }

    public void setSupplier_name(int supplier_name) {
        this.supplier_name = supplier_name;
    }

    public Date getDate_added() {
        return date_added;
    }

    public void setDate_added(Date date_added) {
        this.date_added = date_added;
    }

    public Timestamp getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Timestamp last_updated) {
        this.last_updated = last_updated;
    }

    public int getProduct_brand() {
        return product_brand;
    }

    public void setProduct_brand(int product_brand) {
        this.product_brand = product_brand;
    }

    public int getProduct_category() {
        return product_category;
    }

    public void setProduct_category(int product_category) {
        this.product_category = product_category;
    }

    public int getProduct_segment() {
        return product_segment;
    }

    public void setProduct_segment(int product_segment) {
        this.product_segment = product_segment;
    }

    public int getProduct_section() {
        return product_section;
    }

    public void setProduct_section(int product_section) {
        this.product_section = product_section;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public int getProduct_class() {
        return product_class;
    }

    public void setProduct_class(int product_class) {
        this.product_class = product_class;
    }

    public int getBase_unit() {
        return base_unit;
    }

    public void setBase_unit(int base_unit) {
        this.base_unit = base_unit;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public int getProduct_nature() {
        return product_nature;
    }

    public void setProduct_nature(int product_nature) {
        this.product_nature = product_nature;
    }

    public int getProduct_shelf_life() {
        return product_shelf_life;
    }

    public void setProduct_shelf_life(int product_shelf_life) {
        this.product_shelf_life = product_shelf_life;
    }

    public int getMaintaining_base_quantity() {
        return maintaining_base_quantity;
    }

    public void setMaintaining_base_quantity(int maintaining_base_quantity) {
        this.maintaining_base_quantity = maintaining_base_quantity;
    }

    public Double getProduct_base_weight() {
        return product_base_weight;
    }

    public void setProduct_base_weight(Double product_base_weight) {
        this.product_base_weight = product_base_weight;
    }

    public String getSupplierFromId() {
        return supplierFromId;
    }

    public void setSupplierFromId(String supplierFromId) {
        this.supplierFromId = supplierFromId;
    }

    public String getBrandFromId() {
        return brandFromId;
    }

    public void setBrandFromId(String brandFromId) {
        this.brandFromId = brandFromId;
    }

    public String getCategoryFromId() {
        return categoryFromId;
    }

    public void setCategoryFromId(String categoryFromId) {
        this.categoryFromId = categoryFromId;
    }

    public String getSegmentFromId() {
        return segmentFromId;
    }

    public void setSegmentFromId(String segmentFromId) {
        this.segmentFromId = segmentFromId;
    }

    public String getSectionFromId() {
        return sectionFromId;
    }

    public void setSectionFromId(String sectionFromId) {
        this.sectionFromId = sectionFromId;
    }

    public Product(int product_id, String product_name, String product_code, String description, int supplier_name, Date date_added, Timestamp last_updated, int product_brand, int product_category, int product_segment, int product_section, Boolean isActive, int product_class, int base_unit, String product_image, int product_nature, int product_shelf_life, int maintaining_base_quantity, Double product_base_weight, String supplierFromId, String brandFromId, String categoryFromId, String segmentFromId, String sectionFromId) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_code = product_code;
        this.description = description;
        this.supplier_name = supplier_name;
        this.date_added = date_added;
        this.last_updated = last_updated;
        this.product_brand = product_brand;
        this.product_category = product_category;
        this.product_segment = product_segment;
        this.product_section = product_section;
        this.isActive = isActive;
        this.product_class = product_class;
        this.base_unit = base_unit;
        this.product_image = product_image;
        this.product_nature = product_nature;
        this.product_shelf_life = product_shelf_life;
        this.maintaining_base_quantity = maintaining_base_quantity;
        this.product_base_weight = product_base_weight;
        this.supplierFromId = supplierFromId;
        this.brandFromId = brandFromId;
        this.categoryFromId = categoryFromId;
        this.segmentFromId = segmentFromId;
        this.sectionFromId = sectionFromId;
    }
}