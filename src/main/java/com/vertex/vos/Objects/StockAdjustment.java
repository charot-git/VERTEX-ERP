package com.vertex.vos.Objects;

import lombok.Data;

@Data
public class StockAdjustment {
    private int id;
    private String docNo;  // Added doc_no field
    private Product product;
    private Branch branch;
    private int quantity;
    private AdjustmentType adjustmentType;
    private int createdBy;

    public enum AdjustmentType {
        IN,
        OUT
    }
}
