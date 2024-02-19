package com.vertex.vos.Constructors;

public class ProductsReceiving {
    int poId;
    int branches;
    String branchNames;
    String itemCode;
    String productDescription;
    int unit;
    String unitString;
    int orderedQuantity;
    int receivedQuantity;

    public ProductsReceiving(int poId, int branches, String branchNames, String itemCode, String productDescription, int unit, String unitString, int orderedQuantity, int receivedQuantity) {
        this.poId = poId;
        this.branches = branches;
        this.branchNames = branchNames;
        this.itemCode = itemCode;
        this.productDescription = productDescription;
        this.unit = unit;
        this.unitString = unitString;
        this.orderedQuantity = orderedQuantity;
        this.receivedQuantity = receivedQuantity;
    }

    public int getPoId() {
        return poId;
    }

    public void setPoId(int poId) {
        this.poId = poId;
    }

    public int getBranches() {
        return branches;
    }

    public void setBranches(int branches) {
        this.branches = branches;
    }

    public String getBranchNames() {
        return branchNames;
    }

    public void setBranchNames(String branchNames) {
        this.branchNames = branchNames;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public String getUnitString() {
        return unitString;
    }

    public void setUnitString(String unitString) {
        this.unitString = unitString;
    }

    public int getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public int getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(int receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}
