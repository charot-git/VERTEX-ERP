package com.vertex.vos.Objects;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SalesOrderHeader {
    private String orderId;
    private String customerName;
    private int customerId;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    private int adminId;
    private Timestamp orderDate;
    private String posNo;
    private String terminalNo;
    private int headerId;
    private String status;
    private BigDecimal cash;
    private BigDecimal amountDue;
    private BigDecimal change;
    private Timestamp paidDate;
    private String paidBy;
    private int salesmanId;
    private int sourceBranchId;
    private boolean isInvoice;

    public boolean isInvoice() {
        return isInvoice;
    }

    public void setInvoice(boolean invoice) {
        isInvoice = invoice;
    }

    public int getSourceBranchId() {
        return sourceBranchId;
    }

    public void setSourceBranchId(int sourceBranchId) {
        this.sourceBranchId = sourceBranchId;
    }

    public int getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(int salesmanId) {
        this.salesmanId = salesmanId;
    }

    public SalesOrderHeader() {
    }

    public SalesOrderHeader(String orderId, String customerName, int adminId, Timestamp orderDate, String posNo, String terminalNo, int headerId, String status, BigDecimal cash, BigDecimal amountDue, BigDecimal change, Timestamp paidDate, String paidBy, int salesmanId, int sourceBranchId, boolean isInvoice) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.adminId = adminId;
        this.orderDate = orderDate;
        this.posNo = posNo;
        this.terminalNo = terminalNo;
        this.headerId = headerId;
        this.status = status;
        this.cash = cash;
        this.amountDue = amountDue;
        this.change = change;
        this.paidDate = paidDate;
        this.paidBy = paidBy;
        this.salesmanId = salesmanId;
        this.sourceBranchId = sourceBranchId;
        this.isInvoice = isInvoice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getPosNo() {
        return posNo;
    }

    public void setPosNo(String posNo) {
        this.posNo = posNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public Timestamp getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Timestamp paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }
}
