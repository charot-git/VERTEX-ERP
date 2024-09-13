package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Brands {
    private int brand_id;
    private String brand_name;

    public Brands(int brand_id, String brand_name) {
        this.brand_id = brand_id;
        this.brand_name = brand_name;
    }
}
