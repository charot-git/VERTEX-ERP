package com.vertex.vos.Objects;

import java.sql.Timestamp;

public class ProductInventory {
    private int inventory_id;
    private int product_id;
    private Double quantity;
    private Double price;
    private int unit_of_measurement;
    private int unit_of_measurement_count;
    private Timestamp last_update_timestamp;
    private String barcode;
    private String description;
    private String inventory_picture;
    private int secondary_category;
    private int secondary_segment;
    private Double estimated_unit_cost;
    private Double estimated_extended_cost;

    public int getInventory_id() {
        return inventory_id;
    }

    public void setInventory_id(int inventory_id) {
        this.inventory_id = inventory_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getUnit_of_measurement() {
        return unit_of_measurement;
    }

    public void setUnit_of_measurement(int unit_of_measurement) {
        this.unit_of_measurement = unit_of_measurement;
    }

    public int getUnit_of_measurement_count() {
        return unit_of_measurement_count;
    }

    public void setUnit_of_measurement_count(int unit_of_measurement_count) {
        this.unit_of_measurement_count = unit_of_measurement_count;
    }

    public Timestamp getLast_update_timestamp() {
        return last_update_timestamp;
    }

    public void setLast_update_timestamp(Timestamp last_update_timestamp) {
        this.last_update_timestamp = last_update_timestamp;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInventory_picture() {
        return inventory_picture;
    }

    public void setInventory_picture(String inventory_picture) {
        this.inventory_picture = inventory_picture;
    }

    public int getSecondary_category() {
        return secondary_category;
    }

    public void setSecondary_category(int secondary_category) {
        this.secondary_category = secondary_category;
    }

    public int getSecondary_segment() {
        return secondary_segment;
    }

    public void setSecondary_segment(int secondary_segment) {
        this.secondary_segment = secondary_segment;
    }

    public Double getEstimated_unit_cost() {
        return estimated_unit_cost;
    }

    public void setEstimated_unit_cost(Double estimated_unit_cost) {
        this.estimated_unit_cost = estimated_unit_cost;
    }

    public Double getEstimated_extended_cost() {
        return estimated_extended_cost;
    }

    public void setEstimated_extended_cost(Double estimated_extended_cost) {
        this.estimated_extended_cost = estimated_extended_cost;
    }

    public ProductInventory(int inventory_id, int product_id, Double quantity, Double price, int unit_of_measurement, int unit_of_measurement_count, Timestamp last_update_timestamp, String barcode, String description, String inventory_picture, int secondary_category, int secondary_type, Double estimated_unit_cost, Double estimated_extended_cost) {
        this.inventory_id = inventory_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.price = price;
        this.unit_of_measurement = unit_of_measurement;
        this.unit_of_measurement_count = unit_of_measurement_count;
        this.last_update_timestamp = last_update_timestamp;
        this.barcode = barcode;
        this.description = description;
        this.inventory_picture = inventory_picture;
        this.secondary_category = secondary_category;
        this.secondary_segment = secondary_type;
        this.estimated_unit_cost = estimated_unit_cost;
        this.estimated_extended_cost = estimated_extended_cost;
    }

    public ProductInventory() {

    }

}
