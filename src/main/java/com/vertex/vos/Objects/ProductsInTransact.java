package com.vertex.vos.Objects;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductsInTransact implements Cloneable {
    private int OrderProductId;
    private int orderId;
    private int productId;
    private String description;
    private String unit;
    private int orderedQuantity;
    private int receivedQuantity;
    private double unitPrice;

    private double overridePrice;

    private double discountedPrice;

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public double getOverridePrice() {
        return overridePrice;
    }

    public void setOverridePrice(double overridePrice) {
        this.overridePrice = overridePrice;
    }

    public double getApprovedPrice() {
        return approvedPrice;
    }

    public void setApprovedPrice(double approvedPrice) {
        this.approvedPrice = approvedPrice;
    }

    private double approvedPrice;
    private double vatAmount;
    private double withholdingAmount;
    private double totalAmount; //net amount
    private double paymentAmount;
    private double grossAmount;
    private double discountedAmount;
    private int inventoryQuantity;
    private int reservedQuantity;

    public int getInventoryQuantity() {
        return inventoryQuantity;
    }

    public void setInventoryQuantity(int inventoryQuantity) {
        this.inventoryQuantity = inventoryQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public double getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(double grossAmount) {
        this.grossAmount = grossAmount;
    }

    public double getDiscountedAmount() {
        return discountedAmount;
    }

    public void setDiscountedAmount(double discountedAmount) {
        this.discountedAmount = discountedAmount;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    private int branchId;
    private int discountTypeId;
    private String discountTypeName;

    private String invoiceNo;
    private String serialNo;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Map<Branch, Integer> getBranchQuantities() {
        return branchQuantities;
    }

    public void setBranchQuantities(Map<Branch, Integer> branchQuantities) {
        this.branchQuantities = branchQuantities;
    }

    public Map<String, Integer> getInvoiceQuantities() {
        return invoiceQuantities;
    }

    public void setInvoiceQuantities(Map<String, Integer> invoiceQuantities) {
        this.invoiceQuantities = invoiceQuantities;
    }

    public Map<String, Double> getInvoiceUnitPrice() {
        return invoiceUnitPrice;
    }

    public void setInvoiceUnitPrice(Map<String, Double> invoiceUnitPrice) {
        this.invoiceUnitPrice = invoiceUnitPrice;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getDiscountTypeName() {
        return discountTypeName;
    }

    public void setDiscountTypeName(String discountTypeName) {
        this.discountTypeName = discountTypeName;
    }

    public int getDiscountTypeId() {
        return discountTypeId;
    }

    public void setDiscountTypeId(int discountTypeId) {
        this.discountTypeId = discountTypeId;
    }

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


    public int getOrderProductId() {
        return OrderProductId;
    }

    public void setOrderProductId(int orderProductId) {
        this.OrderProductId = orderProductId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    private String receiptNo;
    private Date receiptDate;

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

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

