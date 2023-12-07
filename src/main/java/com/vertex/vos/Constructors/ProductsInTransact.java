package com.vertex.vos.Constructors;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductsInTransact {
    private int purchaseOrderProductId;
    private int purchaseOrderId;
    private int productId;
    private String description;
    private String unit;
    private int orderedQuantity;
    private int receivedQuantity;
    private double unitPrice;
    private double vatAmount;
    private double withholdingAmount;
    private double totalAmount;
    private int branchId;

    private Map<Branch, Integer> branchQuantities; // Map to store quantities per branch
    public void setBranchQuantity(Branch branch, int quantity) {
        if (branchQuantities == null) {
            branchQuantities = new HashMap<>();
        }
        branchQuantities.put(branch, quantity);
    }

    public int getBranchQuantity(Branch branch) {
        if (branchQuantities != null && branchQuantities.containsKey(branch)) {
            return branchQuantities.get(branch);
        }
        return 0; // Return 0 if the quantity for the branch is not set
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ProductsInTransact(int purchaseOrderProductId, int purchaseOrderId, int productId, String description, String unit, int orderedQuantity, int receivedQuantity, double unitPrice, double vatAmount, double withholdingAmount, double totalAmount, int branchId) {
        this.purchaseOrderProductId = purchaseOrderProductId;
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
        this.description = description;
        this.unit = unit;
        this.orderedQuantity = orderedQuantity;
        this.receivedQuantity = receivedQuantity;
        this.unitPrice = unitPrice;
        this.vatAmount = vatAmount;
        this.withholdingAmount = withholdingAmount;
        this.totalAmount = totalAmount;
        this.branchId = branchId;
    }

    public ProductsInTransact() {

    }


    public int getPurchaseOrderProductId() {
        return purchaseOrderProductId;
    }

    public void setPurchaseOrderProductId(int purchaseOrderProductId) {
        this.purchaseOrderProductId = purchaseOrderProductId;
    }

    public int getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(int purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getWithholdingAmount() {
        return withholdingAmount;
    }

    public void setWithholdingAmount(double withholdingAmount) {
        this.withholdingAmount = withholdingAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }
}

