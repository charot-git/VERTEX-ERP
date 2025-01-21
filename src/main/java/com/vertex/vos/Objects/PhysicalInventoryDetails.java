package com.vertex.vos.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryDetails {
    private int id;
    private PhysicalInventory physicalInventory; // Reference to PhysicalInventory object
    private Timestamp dateEncoded;
    private Product product; // Reference to Product object
    private double unitPrice;
    private int systemCount;
    private int physicalCount;
    private int variance;
    private double differenceCost;
    private double amount;
}
