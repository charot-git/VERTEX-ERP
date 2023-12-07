package com.vertex.vos.Constructors;

public class PaymentTerms {
    private int id;
    private String paymentName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    public int getPaymentDays() {
        return paymentDays;
    }

    public void setPaymentDays(int paymentDays) {
        this.paymentDays = paymentDays;
    }

    public PaymentTerms(int id, String paymentName, int paymentDays) {
        this.id = id;
        this.paymentName = paymentName;
        this.paymentDays = paymentDays;
    }

    private int paymentDays;
}