package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiscountType {
    // Getter and setter for typeName
    private String typeName;
    private int id;

    // Constructor
    public DiscountType(String typeName, int id) {
        this.typeName = typeName;
    }

    public DiscountType(int id) {
        this.id = id;
    }

    public DiscountType() {

    }
}
