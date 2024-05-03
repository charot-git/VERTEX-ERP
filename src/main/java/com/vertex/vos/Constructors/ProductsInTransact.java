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

    private double overridePrice;

    private double discountedPrice;//net price

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

    private String supplierInvoice;
    private Map<Integer, Integer> invoiceQuantities; // Map to store quantities per invoice

    public void setInvoiceQuantity(int invoiceId, int quantity) {
        if (invoiceQuantities == null) {
            invoiceQuantities = new HashMap<>();
        }
        invoiceQuantities.put(invoiceId, quantity);
    }

    public int getInvoiceQuantity(int invoiceId) {
        if (invoiceQuantities != null && invoiceQuantities.containsKey(invoiceId)) {
            return invoiceQuantities.get(invoiceId);
        }
        return 0; // Return 0 if the quantity for the invoice is not set
    }

    public String getSupplierInvoice() {
        return supplierInvoice;
    }

    public void setSupplierInvoice(String supplierInvoice) {
        this.supplierInvoice = supplierInvoice;
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
}

