package com.vertex.vos.Objects;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class LineDiscount {
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

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLineDiscount() {
        return lineDiscount;
    }

    public void setLineDiscount(String lineDiscount) {
        this.lineDiscount = lineDiscount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
