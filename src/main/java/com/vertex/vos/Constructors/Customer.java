package com.vertex.vos.Constructors;

import java.sql.Timestamp;

public class Customer {
    private int id;
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
    private byte isActive;
    private byte isVAT;
    private byte isEWT;
    private String otherDetails;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public byte getIsActive() {
        return isActive;
    }

    public void setIsActive(byte isActive) {
        this.isActive = isActive;
    }

    public byte getIsVAT() {
        return isVAT;
    }

    public void setIsVAT(byte isVAT) {
        this.isVAT = isVAT;
    }

    public byte getIsEWT() {
        return isEWT;
    }

    public void setIsEWT(byte isEWT) {
        this.isEWT = isEWT;
    }

    public String getOtherDetails() {
        return otherDetails;
    }

    public void setOtherDetails(String otherDetails) {
        this.otherDetails = otherDetails;
    }

    public Customer(int id, String customerCode, String customerName, String customerImage, String storeName, String storeSignage, String brgy, String city, String province, String contactNumber, String customerEmail, String telNumber, String customerTin, byte paymentTerm, int storeType, int discountId, int encoderId, Timestamp dateEntered, byte creditType, byte companyCode, byte isActive, byte isVAT, byte isEWT, String otherDetails) {
        this.id = id;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.customerImage = customerImage;
        this.storeName = storeName;
        this.storeSignage = storeSignage;
        this.brgy = brgy;
        this.city = city;
        this.province = province;
        this.contactNumber = contactNumber;
        this.customerEmail = customerEmail;
        this.telNumber = telNumber;
        this.customerTin = customerTin;
        this.paymentTerm = paymentTerm;
        this.storeType = storeType;
        this.discountId = discountId;
        this.encoderId = encoderId;
        this.dateEntered = dateEntered;
        this.creditType = creditType;
        this.companyCode = companyCode;
        this.isActive = isActive;
        this.isVAT = isVAT;
        this.isEWT = isEWT;
        this.otherDetails = otherDetails;
    }
}

