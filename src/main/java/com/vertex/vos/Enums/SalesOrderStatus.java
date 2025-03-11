package com.vertex.vos.Enums;

import lombok.Getter;

@Getter
public enum SalesOrderStatus {
    REQUESTED("Requested"),
    APPROVED("Approved"),
    ALLOCATED("Allocated"),
    PICKED("Picked"),
    INVOICED("Invoiced"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    ON_HOLD("On Hold");

    private final String dbValue;

    SalesOrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    // Convert from DB value to Enum safely
    public static SalesOrderStatus fromDbValue(String dbValue) {
        for (SalesOrderStatus status : SalesOrderStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + dbValue);
    }
}
