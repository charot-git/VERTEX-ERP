package com.vertex.vos.Objects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PurchaseOrder {
    private int purchaseOrderId;

    public PurchaseOrder() {

    }

    public PurchaseOrder(int purchaseOrderNo, String supplierName, String receivingType, String paymentType, String priceType, LocalDateTime dateEncoded, LocalDateTime dateApproved, LocalDateTime dateReceived, int encoderId, int approverId, int receiverId, String transactionType, String inventoryStatus, LocalDate date, LocalTime time, LocalDateTime datetime, boolean receiptRequired) {
    }

    public int getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(int purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public int getPurchaseOrderNo() {
        return purchaseOrderNo;
    }

    public void setPurchaseOrderNo(int purchaseOrderNo) {
        this.purchaseOrderNo = purchaseOrderNo;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(int supplierName) {
        this.supplierName = supplierName;
    }

    public int getReceivingType() {
        return receivingType;
    }

    public void setReceivingType(int receivingType) {
        this.receivingType = receivingType;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Boolean getReceiptRequired() {
        return receiptRequired;
    }

    public void setReceiptRequired(Boolean receiptRequired) {
        this.receiptRequired = receiptRequired;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getWithholdingTaxAmount() {
        return withholdingTaxAmount;
    }

    public void setWithholdingTaxAmount(BigDecimal withholdingTaxAmount) {
        this.withholdingTaxAmount = withholdingTaxAmount;
    }

    public LocalDateTime getDateEncoded() {
        return dateEncoded;
    }

    public void setDateEncoded(LocalDateTime dateEncoded) {
        this.dateEncoded = dateEncoded;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getEncoderId() {
        return encoderId;
    }

    public void setEncoderId(int encoderId) {
        this.encoderId = encoderId;
    }

    public int getApproverId() {
        return approverId;
    }

    public void setApproverId(int approverId) {
        this.approverId = approverId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getFinanceId() {
        return financeId;
    }

    public void setFinanceId(int financeId) {
        this.financeId = financeId;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(LocalDateTime dateApproved) {
        this.dateApproved = dateApproved;
    }

    public LocalDateTime getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(LocalDateTime dateReceived) {
        this.dateReceived = dateReceived;
    }

    public LocalDateTime getDateFinanced() {
        return dateFinanced;
    }

    public void setDateFinanced(LocalDateTime dateFinanced) {
        this.dateFinanced = dateFinanced;
    }

    public LocalDateTime getDateVouchered() {
        return dateVouchered;
    }

    public void setDateVouchered(LocalDateTime dateVouchered) {
        this.dateVouchered = dateVouchered;
    }

    public int getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(int inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }
    private int purchaseOrderNo;
    private String reference;
    private String remark;
    private String barcode;
    private int supplierName;
    private int receivingType;
    private int paymentType;
    private String priceType;
    private Boolean receiptRequired;
    private BigDecimal vatAmount;
    private BigDecimal withholdingTaxAmount;
    private LocalDateTime dateEncoded;

    private LocalDate date;
    private LocalDate leadTimeReceiving;
    private LocalDate leadTimePayment;
    private LocalTime time;

    public LocalDate getLeadTimeReceiving() {
        return leadTimeReceiving;
    }

    public void setLeadTimeReceiving(LocalDate leadTimeReceiving) {
        this.leadTimeReceiving = leadTimeReceiving;
    }

    public LocalDate getLeadTimePayment() {
        return leadTimePayment;
    }

    public void setLeadTimePayment(LocalDate leadTimePayment) {
        this.leadTimePayment = leadTimePayment;
    }

    private LocalDateTime datetime;

    public BigDecimal getTotalGrossAmount() {
        return totalGrossAmount;
    }

    public void setTotalGrossAmount(BigDecimal totalGrossAmount) {
        this.totalGrossAmount = totalGrossAmount;
    }
    private BigDecimal totalGrossAmount;

    public BigDecimal getTotalDiscountedAmount() {
        return totalDiscountedAmount;
    }

    public void setTotalDiscountedAmount(BigDecimal totalDiscountedAmount) {
        this.totalDiscountedAmount = totalDiscountedAmount;
    }

    private BigDecimal totalDiscountedAmount;

    private BigDecimal totalAmount;
    private int encoderId;
    private int approverId;
    private int receiverId;
    private int financeId;
    private int voucherId;
    private int transactionType;
    private LocalDateTime dateApproved;
    private LocalDateTime dateReceived;
    private LocalDateTime dateFinanced;
    private LocalDateTime dateVouchered;
    private int inventoryStatus;
    private String inventoryStatusString;
    private int paymentStatus;

    public String getPaymentStatusString() {
        return paymentStatusString;
    }

    public void setPaymentStatusString(String paymentStatusString) {
        this.paymentStatusString = paymentStatusString;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    private String paymentStatusString;

    public String getInventoryStatusString() {
        return inventoryStatusString;
    }

    public void setInventoryStatusString(String inventoryStatusString) {
        this.inventoryStatusString = inventoryStatusString;
    }

    private String transactionTypeString;

    public String getSupplierNameString() {
        return supplierNameString;
    }

    public void setSupplierNameString(String supplierNameString) {
        this.supplierNameString = supplierNameString;
    }

    private String supplierNameString;

    public String getTransactionTypeString() {
        return transactionTypeString;
    }

    public void setTransactionTypeString(String transactionTypeString) {
        this.transactionTypeString = transactionTypeString;
    }

    public PurchaseOrder(String transactionTypeString) {
        this.transactionTypeString = transactionTypeString;
    }
}
