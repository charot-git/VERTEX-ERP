package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ProductsInTransact implements Cloneable {
    private int OrderProductId;
    private int orderId;
    private int productId;
    private String description;

    private String productCategoryString;
    private String productBrandString;
    private Product product;

    private String unit;
    private int orderedQuantity;
    private int receivedQuantity;
    private int availableQuantity;
    private double unitPrice;
    private double overridePrice;

    private double discountedPrice;
    private boolean discountApplied; // Add the discountApplied flag

    private double approvedPrice;
    private double vatAmount;
    private double withholdingAmount;
    private double totalAmount; //net amount
    private double paymentAmount;
    private double grossAmount;
    private double discountedAmount;
    private int inventoryQuantity;
    private int reservedQuantity;

    private int branchId;
    private int discountTypeId;
    private String discountTypeName;

    private String invoiceNo;
    private String serialNo;

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

    public ProductsInTransact(int purchaseOrderProductId, int purchaseOrderId, int productId, String description, String unit, int orderedQuantity, int receivedQuantity, double unitPrice, double vatAmount, double withholdingAmount, double totalAmount, int branchId) {
        this.OrderProductId = purchaseOrderProductId;
        this.orderId = purchaseOrderId;
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


    private String receiptNo;
    private Date receiptDate;

    public void setTotalReceivedQuantity(int totalReceivedForProduct) {
        this.receivedQuantity = totalReceivedForProduct;
    }

    private Map<String, Integer> invoiceQuantities;

    private Map<String, Double> invoiceUnitPrice;


    // Method to set the received quantity for a specific invoice
    public void setReceivedQuantityForInvoice(String invoiceNumber, int receivedQuantityForProductAndInvoice) {
        // Implement the logic to set the received quantity for the specified invoice number
        if (invoiceQuantities == null) {
            invoiceQuantities = new HashMap<>();
        }
        invoiceQuantities.put(invoiceNumber, receivedQuantityForProductAndInvoice);
    }

    @Override
    public ProductsInTransact clone() {
        try {
            return (ProductsInTransact) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

