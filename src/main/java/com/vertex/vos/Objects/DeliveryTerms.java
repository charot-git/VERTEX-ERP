package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeliveryTerms {
    private int id;
    private String deliveryName;

    public DeliveryTerms() {

    }

    public DeliveryTerms(int id, String deliveryName) {
        this.id = id;
        this.deliveryName = deliveryName;
    }
}
