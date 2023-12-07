package com.vertex.vos.Constructors;

public class DeliveryTerms {
    private int id;
    private String deliveryName;

    public DeliveryTerms() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public DeliveryTerms(int id, String deliveryName) {
        this.id = id;
        this.deliveryName = deliveryName;
    }
}
