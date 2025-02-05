package com.vertex.vos.Objects;

import lombok.Setter;

public class ProductBreakdown {
    private int productId;
    private int unitId;
    private String unitName;
    private String unitShortcut;
    private int order;
    private String description;
    private int unitCount;

    // Setters for Conversion Product IDs
    // Additional fields for conversion
    @Setter
    private int productIdToConvert;
    @Setter
    private int productIdForConversion;

    public ProductBreakdown() {}

    public ProductBreakdown(int productId, int unitId, String unitName, String unitShortcut, int order, String description) {
        this.productId = productId;
        this.unitId = unitId;
        this.unitName = unitName;
        this.unitShortcut = unitShortcut;
        this.order = order;
        this.description = description;
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getUnitShortcut() {
        return unitShortcut;
    }

    public int getOrder() {
        return order;
    }

    public String getDescription() {
        return description;
    }

    public int getUnitCount() {
        return unitCount;
    }

    // New Getters for Conversion Product IDs
    public int getProductIdToConvert() {
        return productIdToConvert;
    }

    public int getProductIdForConversion() {
        return productIdForConversion;
    }

    // Conversion Ratio Method
    public double getConversionRatio(ProductBreakdown otherProduct) {
        if (otherProduct != null && this.unitCount > 0 && otherProduct.getUnitCount() > 0) {
            return (double) this.unitCount / otherProduct.getUnitCount();
        } else {
            return 1.0; // Default ratio if there is no valid comparison
        }
    }

}
