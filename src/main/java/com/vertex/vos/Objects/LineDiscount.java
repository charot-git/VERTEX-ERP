package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class LineDiscount {
    // Getters and setters
    private int id;
    private String lineDiscount;
    private BigDecimal percentage;

    // Constructor
    public LineDiscount(int id, String lineDiscount, BigDecimal percentage) {
        this.id = id;
        this.lineDiscount = lineDiscount;
        this.percentage = percentage;
    }

    public static List<String> getPropertyNames() {
        return Arrays.asList("lineId", "lineDiscountName", "discountValue");
    }

}
