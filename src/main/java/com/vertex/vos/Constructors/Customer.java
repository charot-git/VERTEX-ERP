package com.vertex.vos.Constructors;

import java.sql.Timestamp;

public class Customer {


    private int customerId;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    private String customerCode;
    private String customerName;
    private String customerImage;
    private String storeName;
    private String storeSignage;
    private String brgy;
    private String city;
    private String province;
    private String contactNumber;
    private String customerEmail;
    private String telNumber;
    private String customerTin;
    private byte paymentTerm;
    private int storeType;
    private int discountId;
    private int encoderId;
    private Timestamp dateEntered;
    private byte creditType;
    private byte companyCode;
    private String priceType;
    private boolean isActive;
    private boolean isVAT;
    private boolean isEWT;
    private String otherDetails;

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Customer() {

    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public void setCustomerImage(String customerImage) {
        this.customerImage = customerImage;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreSignage() {
        return storeSignage;
    }

    public void setStoreSignage(String storeSignage) {
        this.storeSignage = storeSignage;
    }

    public String getBrgy() {
        return brgy;
    }

    public void setBrgy(String brgy) {
        this.brgy = brgy;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getCustomerTin() {
        return customerTin;
    }

    public void setCustomerTin(String customerTin) {
        this.customerTin = customerTin;
    }

    public byte getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(byte paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public int getStoreType() {
        return storeType;
    }

    public void setStoreType(int storeType) {
        this.storeType = storeType;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public int getEncoderId() {
        return encoderId;
    }

    public void setEncoderId(int encoderId) {
        this.encoderId = encoderId;
    }

    public Timestamp getDateEntered() {
        return dateEntered;
    }

    public void setDateEntered(Timestamp dateEntered) {
        this.dateEntered = dateEntered;
    }

    public byte getCreditType() {
        return creditType;
    }

    public void setCreditType(byte creditType) {
        this.creditType = creditType;
    }

    public byte getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(byte companyCode) {
        this.companyCode = companyCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isVAT() {
        return isVAT;
    }

    public void setVAT(boolean VAT) {
        isVAT = VAT;
    }

    public boolean isEWT() {
        return isEWT;
    }

    public void setEWT(boolean EWT) {
        isEWT = EWT;
    }

    public String getOtherDetails() {
        return otherDetails;
    }

    public void setOtherDetails(String otherDetails) {
        this.otherDetails = otherDetails;
    }
}

