package com.vertex.vos.Constructors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PurchaseOrder {
    private int purchaseOrderId;

    public PurchaseOrder() {

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PurchaseOrder(int purchaseOrderId, int purchaseOrderNo, String reference, String remark, String barcode, int supplierName, int receivingType, int paymentType, String priceType, Boolean receiptRequired, BigDecimal vatAmount, BigDecimal withholdingTaxAmount, LocalDateTime dateEncoded, LocalDate date, LocalTime time, LocalDateTime datetime, BigDecimal totalAmount, int encoderId, int approverId, int receiverId, int financeId, int voucherId, int transactionType, LocalDateTime dateApproved, LocalDateTime dateReceived, LocalDateTime dateFinanced, LocalDateTime dateVouchered, int status) {
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderNo = purchaseOrderNo;
        this.reference = reference;
        this.remark = remark;
        this.barcode = barcode;
        this.supplierName = supplierName;
        this.receivingType = receivingType;
        this.paymentType = paymentType;
        this.priceType = priceType;
        this.receiptRequired = receiptRequired;
        this.vatAmount = vatAmount;
        this.withholdingTaxAmount = withholdingTaxAmount;
        this.dateEncoded = dateEncoded;
        this.date = date;
        this.time = time;
        this.datetime = datetime;
        this.totalAmount = totalAmount;
        this.encoderId = encoderId;
        this.approverId = approverId;
        this.receiverId = receiverId;
        this.financeId = financeId;
        this.voucherId = voucherId;
        this.transactionType = transactionType;
        this.dateApproved = dateApproved;
        this.dateReceived = dateReceived;
        this.dateFinanced = dateFinanced;
        this.dateVouchered = dateVouchered;
        this.status = status;
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
    private LocalTime time;
    private LocalDateTime datetime;
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
    private int status;
    private String statusString;

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
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
