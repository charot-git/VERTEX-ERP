package com.vertex.vos.Objects;

import java.sql.Date;
import java.sql.Timestamp;

public class SalesInvoiceHeader {
    private int invoiceId;
    private String orderId;
    private String customerCode;
    private int salesmanId;
    private Date invoiceDate;
    private Date dueDate;
    private int paymentTerms;
    private String transactionStatus;
    private String paymentStatus;
    private double totalAmount;
    private int salesType;
    private double vatAmount;
    private double discountAmount;
    private double netAmount;
    private int createdBy;
    private Timestamp createdDate;
    private int modifiedBy;
    private Timestamp modifiedDate;
    private int postedBy;
    private Date postedDate;
    private int isReceipt;
    private int type;
    private String remarks;
    private String customerName;
    private String storeName;
    private Salesman salesman;

    public Salesman getSalesman() {
        return salesman;
    }

    public void setSalesman(Salesman salesman) {
        this.salesman = salesman;
    }

    public SalesInvoiceHeader(int invoiceId, String orderId, String customerCode, int salesmanId, Date invoiceDate, Date dueDate, int paymentTerms, String transactionStatus, String paymentStatus, double totalAmount, int salesType, double vatAmount, double discountAmount, double netAmount, int createdBy, Timestamp createdDate, int modifiedBy, Timestamp modifiedDate, int postedBy, Date postedDate, int isReceipt, int type, String remarks) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.customerCode = customerCode;
        this.salesmanId = salesmanId;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.paymentTerms = paymentTerms;
        this.transactionStatus = transactionStatus;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.salesType = salesType;
        this.vatAmount = vatAmount;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.postedBy = postedBy;
        this.postedDate = postedDate;
        this.isReceipt = isReceipt;
        this.type = type;
        this.remarks = remarks;
    }

    public SalesInvoiceHeader() {

    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public int getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(int salesmanId) {
        this.salesmanId = salesmanId;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(int paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getSalesType() {
        return salesType;
    }

    public void setSalesType(int salesType) {
        this.salesType = salesType;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(int postedBy) {
        this.postedBy = postedBy;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public int getIsReceipt() {
        return isReceipt;
    }

    public void setIsReceipt(int isReceipt) {
        this.isReceipt = isReceipt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
