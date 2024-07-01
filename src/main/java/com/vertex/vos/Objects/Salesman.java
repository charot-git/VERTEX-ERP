package com.vertex.vos.Objects;

import java.time.LocalDateTime;

public class Salesman {
    private int id;
    private int employeeId;
    private String salesmanCode;
    private String salesmanName;
    private String truckPlate;
    private int divisionId;
    private int branchCode;
    private int operation;
    private int companyCode;
    private int supplierCode;
    private String priceType;
    private boolean isActive;
    private boolean isInventory;
    private boolean canCollect;
    private int inventoryDay;

    public Salesman() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getSalesmanCode() {
        return salesmanCode;
    }

    public void setSalesmanCode(String salesmanCode) {
        this.salesmanCode = salesmanCode;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public String getTruckPlate() {
        return truckPlate;
    }

    public void setTruckPlate(String truckPlate) {
        this.truckPlate = truckPlate;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public int getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(int branchCode) {
        this.branchCode = branchCode;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(int companyCode) {
        this.companyCode = companyCode;
    }

    public int getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(int supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isInventory() {
        return isInventory;
    }

    public void setInventory(boolean inventory) {
        isInventory = inventory;
    }

    public boolean isCanCollect() {
        return canCollect;
    }

    public void setCanCollect(boolean canCollect) {
        this.canCollect = canCollect;
    }

    public int getInventoryDay() {
        return inventoryDay;
    }

    public void setInventoryDay(int inventoryDay) {
        this.inventoryDay = inventoryDay;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getEncoderId() {
        return encoderId;
    }

    public void setEncoderId(int encoderId) {
        this.encoderId = encoderId;
    }

    public Salesman(int id, int employeeId, String salesmanCode, String salesmanName, String truckPlate, int divisionId, int branchCode, int operation, int companyCode, int supplierCode, String priceType, boolean isActive, boolean isInventory, boolean canCollect, int inventoryDay, LocalDateTime modifiedDate, int encoderId) {
        this.id = id;
        this.employeeId = employeeId;
        this.salesmanCode = salesmanCode;
        this.salesmanName = salesmanName;
        this.truckPlate = truckPlate;
        this.divisionId = divisionId;
        this.branchCode = branchCode;
        this.operation = operation;
        this.companyCode = companyCode;
        this.supplierCode = supplierCode;
        this.priceType = priceType;
        this.isActive = isActive;
        this.isInventory = isInventory;
        this.canCollect = canCollect;
        this.inventoryDay = inventoryDay;
        this.modifiedDate = modifiedDate;
        this.encoderId = encoderId;
    }

    private LocalDateTime modifiedDate;
    private int encoderId;
}
