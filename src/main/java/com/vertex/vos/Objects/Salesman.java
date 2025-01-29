package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salesman {
    private int id;
    private int employeeId;
    private String salesmanCode;
    private String salesmanName;
    private String truckPlate;
    private int divisionId;
    private int goodBranchCode;
    private int badBranchCode;
    private int operation;
    private int companyCode;
    private int supplierCode;
    private String priceType;
    private boolean isActive;
    private boolean isInventory;
    private boolean canCollect;
    private int inventoryDay;
    private int encoderId;
    private LocalDateTime modifiedDate;
}