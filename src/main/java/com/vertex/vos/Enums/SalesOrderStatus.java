package com.vertex.vos.Enums;

import lombok.Getter;

@Getter
public enum SalesOrderStatus {
    FOR_APPROVAL("For Approval", "#fdae61"),      // Orange
    FOR_CONSOLIDATION("For Consolidation", "#fee08b"), // Gold
    FOR_PICKING("For Picking", "#ffffbf"),        // Dark Turquoise
    FOR_INVOICING("For Invoicing", "#e6f598"),    // Steel Blue
    FOR_LOADING("For Loading", "#abdda4"),        // Blue Violet
    FOR_SHIPPING("For Shipping", "#66c2a5"),      // Dodger Blue
    DELIVERED("Delivered", "#3288bd"),            // Green
    ON_HOLD("On Hold", "#f46d43"),                // Orange Red
    CANCELLED("Cancelled", "#d53e4f");            // Crimson

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
