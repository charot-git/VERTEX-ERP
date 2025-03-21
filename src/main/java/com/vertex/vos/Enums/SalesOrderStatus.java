package com.vertex.vos.Enums;

import lombok.Getter;

@Getter
public enum SalesOrderStatus {
    FOR_APPROVAL("For Approval", "#FFA500"),      // Orange
    FOR_CONSOLIDATION("For Consolidation", "#FFD700"), // Gold
    FOR_PICKING("For Picking", "#00CED1"),        // Dark Turquoise
    FOR_INVOICING("For Invoicing", "#4682B4"),    // Steel Blue
    FOR_LOADING("For Loading", "#8A2BE2"),        // Blue Violet
    FOR_SHIPPING("For Shipping", "#1E90FF"),      // Dodger Blue
    DELIVERED("Delivered", "#008000"),            // Green
    ON_HOLD("On Hold", "#FF4500"),                // Orange Red
    CANCELLED("Cancelled", "#DC143C");            // Crimson

    private final String dbValue;
    @Getter
    private final String color;

    SalesOrderStatus(String dbValue, String color) {
        this.dbValue = dbValue;
        this.color = color;
    }

    public static SalesOrderStatus fromDbValue(String dbValue) {
        for (SalesOrderStatus status : SalesOrderStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + dbValue);
    }

}
