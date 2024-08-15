package com.vertex.vos.Objects;

public class ProductBreakdown {
    private final int productId;
    private final int unitId;
    private final String unitName;
    private final String unitShortcut;
    private final int order;
    private final String description;

    public int getProductId() {
        return productId;
    }

    public ProductBreakdown(int productId, int unitId, String unitName, String unitShortcut, int order, String description) {
        this.productId = productId;
        this.unitId = unitId;
        this.unitName = unitName;
        this.unitShortcut = unitShortcut;
        this.order = order;
        this.description = description;
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
}
