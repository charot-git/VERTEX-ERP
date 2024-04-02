package com.vertex.vos.Constructors;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SalesOrder {
    private int salesOrderId;
    private String orderId;
    private int productId;
    private String description;
    private String barcode;
    private BigDecimal qty;
    private BigDecimal price;
    private String tabName;
    private String customerId;

    public SalesOrder(int salesOrderId, String orderId, int productId, String description, String barcode, BigDecimal qty, BigDecimal price, String tabName, String customerId, String customerName, String storeName, String salesMan, Timestamp createdDate, BigDecimal total, String soStatus) {
        this.salesOrderId = salesOrderId;
        this.orderId = orderId;
        this.productId = productId;
        this.description = description;
        this.barcode = barcode;
        this.qty = qty;
        this.price = price;
        this.tabName = tabName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.storeName = storeName;
        this.salesMan = salesMan;
        this.createdDate = createdDate;
        this.total = total;
        this.soStatus = soStatus;
    }

    public SalesOrder() {

    }

    public int getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(int salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getSalesMan() {
        return salesMan;
    }

    public void setSalesMan(String salesMan) {
        this.salesMan = salesMan;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getSoStatus() {
        return soStatus;
    }

    public void setSoStatus(String soStatus) {
        this.soStatus = soStatus;
    }

    private String customerName;
    private String storeName;
    private String salesMan;
    private Timestamp createdDate;
    private BigDecimal total;
    private String soStatus;
}
