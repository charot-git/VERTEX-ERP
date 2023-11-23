package com.vertex.vos.Constructors;

import java.sql.Timestamp;

public class PurchaseOrder {

    private int id;
    private String poId;
    private String branchName;
    private String supplierName;
    private String transactionType;
    private Timestamp dateEncoded;
    private int encoderId;
    private int approverId;
    private int receiverId;

    private String encoderName;
    private String approverName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Timestamp getDateEncoded() {
        return dateEncoded;
    }

    public void setDateEncoded(Timestamp dateEncoded) {
        this.dateEncoded = dateEncoded;
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

    public String getEncoderName() {
        return encoderName;
    }

    public void setEncoderName(String encoderName) {
        this.encoderName = encoderName;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getWithholdingTaxAmount() {
        return withholdingTaxAmount;
    }

    public void setWithholdingTaxAmount(double withholdingTaxAmount) {
        this.withholdingTaxAmount = withholdingTaxAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PurchaseOrder(int id, String poId, String branchName, String supplierName, String transactionType, Timestamp dateEncoded, int encoderId, int approverId, int receiverId, String encoderName, String approverName, String receiverName, double totalAmount, double vatAmount, double withholdingTaxAmount, String status) {
        this.id = id;
        this.poId = poId;
        this.branchName = branchName;
        this.supplierName = supplierName;
        this.transactionType = transactionType;
        this.dateEncoded = dateEncoded;
        this.encoderId = encoderId;
        this.approverId = approverId;
        this.receiverId = receiverId;
        this.encoderName = encoderName;
        this.approverName = approverName.isEmpty() ? "NOT APPROVED YET" : approverName;
        this.receiverName = receiverName;
        this.totalAmount = totalAmount;
        this.vatAmount = vatAmount;
        this.withholdingTaxAmount = withholdingTaxAmount;
        this.status = status;
    }

    private String receiverName;
    private double totalAmount;
    private double vatAmount;
    private double withholdingTaxAmount;
    private String status;
}