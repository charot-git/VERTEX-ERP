package com.vertex.vos.Objects;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Inventory {
    private int branchId;
    private String branchName;
    private int productId;
    private Product product;
    private String productDescription;
    private int quantity;
    private double unitPrice;
    private LocalDateTime lastRestockDate;
    private int reservedQuantity; // Add this fieldprivate String brand; // Added field
    private String unit;
    private String brand;
    private String category; // Added field
    private String productClass; // Added field
    private String productSegment; // Added field
    private String productSection; // Added field
    private String productNature; // Added field

}
