package com.vertex.vos.Objects;

import java.time.LocalDateTime;

public class AssetsAndEquipment {
    private int id;
    private String itemImage;
    private String itemName;
    private int quantity;
    private int department;
    private int employee;
    private double costPerItem;
    private double total;
    private int condition;
    private int lifeSpan;
    private int encoder;

    public AssetsAndEquipment() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public int getEmployee() {
        return employee;
    }

    public void setEmployee(int employee) {
        this.employee = employee;
    }

    public double getCostPerItem() {
        return costPerItem;
    }

    public void setCostPerItem(double costPerItem) {
        this.costPerItem = costPerItem;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public int getEncoder() {
        return encoder;
    }

    public void setEncoder(int encoder) {
        this.encoder = encoder;
    }

    public LocalDateTime getDateAcquired() {
        return dateAcquired;
    }

    public void setDateAcquired(LocalDateTime dateAcquired) {
        this.dateAcquired = dateAcquired;
    }

    public AssetsAndEquipment(int id, String itemImage, String itemName, int quantity, int department, int employee, double costPerItem, double total, int condition, int lifeSpan, int encoder, LocalDateTime dateAcquired) {
        this.id = id;
        this.itemImage = itemImage;
        this.itemName = itemName;
        this.quantity = quantity;
        this.department = department;
        this.employee = employee;
        this.costPerItem = costPerItem;
        this.total = total;
        this.condition = condition;
        this.lifeSpan = lifeSpan;
        this.encoder = encoder;
        this.dateAcquired = dateAcquired;
    }

    private LocalDateTime dateAcquired;
}
