package com.vertex.vos.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventory {
    private int id;
    private String phNo;
    private Timestamp dateEncoded;
    private Timestamp cutOffDate;
    private String priceType;
    private String stockType;
    private Branch branch;
    private String remarks;
    private boolean isCommitted;
    private boolean isCancelled;
    private double totalAmount;
    private Supplier supplier;
    private Category category;
    private int encoderId;
}
