package com.vertex.vos.Constructors;

import org.w3c.dom.Text;

import java.sql.Date;

public class Supplier {
    private int id;
    private String supplierName;
    private String contactPerson;
    private String emailAddress;
    private String phoneNumber;
    private String address;
    private String city;
    private String barangay;
    private String stateProvince;
    private String postalCode;
    private String country;
    private int discountType;
    private String supplierType;
    private String tinNumber;
    private String bankDetails;
    private String productsOrServices;
    private String paymentTerms;
    private String deliveryTerms;
    private String agreementOrContract;
    private String preferredCommunicationMethod;
    private String notesOrComments;
    private Date dateAdded;
    private String supplierImage;

    public Supplier(int id, String supplierName, String contactPerson, String emailAddress,
                    String phoneNumber, String address, String city, String barangay,
                    String stateProvince, String postalCode, String country, int discountType,
                    String supplierType, String tinNumber, String bankDetails,
                    String productsOrServices, String paymentTerms, String deliveryTerms,
                    String agreementOrContract, String preferredCommunicationMethod,
                    String notesOrComments, Date dateAdded, String supplierImage) {
        this.id = id;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.barangay = barangay;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.country = country;
        this.discountType = discountType;
        this.supplierType = supplierType;
        this.tinNumber = tinNumber;
        this.bankDetails = bankDetails;
        this.productsOrServices = productsOrServices;
        this.paymentTerms = paymentTerms;
        this.deliveryTerms = deliveryTerms;
        this.agreementOrContract = agreementOrContract;
        this.preferredCommunicationMethod = preferredCommunicationMethod;
        this.notesOrComments = notesOrComments;
        this.dateAdded = dateAdded;
        this.supplierImage = supplierImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getDiscountType() {
        return discountType;
    }

    public void setDiscountType(int discountType) {
        this.discountType = discountType;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public void setTinNumber(String tinNumber) {
        this.tinNumber = tinNumber;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getProductsOrServices() {
        return productsOrServices;
    }

    public void setProductsOrServices(String productsOrServices) {
        this.productsOrServices = productsOrServices;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getAgreementOrContract() {
        return agreementOrContract;
    }

    public void setAgreementOrContract(String agreementOrContract) {
        this.agreementOrContract = agreementOrContract;
    }

    public String getPreferredCommunicationMethod() {
        return preferredCommunicationMethod;
    }

    public void setPreferredCommunicationMethod(String preferredCommunicationMethod) {
        this.preferredCommunicationMethod = preferredCommunicationMethod;
    }

    public String getNotesOrComments() {
        return notesOrComments;
    }

    public void setNotesOrComments(String notesOrComments) {
        this.notesOrComments = notesOrComments;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getSupplierImage() {
        return supplierImage;
    }

    public void setSupplierImage(String supplierImage) {
        this.supplierImage = supplierImage;
    }

    // Getters and setters for the fields
    // ...
}
