package com.vertex.vos.Constructors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class QuantitySummary {
    private final SimpleStringProperty description;
    private final SimpleStringProperty unit;
    private final SimpleIntegerProperty totalQuantity;

    public QuantitySummary(String description, String unit, int totalQuantity) {
        this.description = new SimpleStringProperty(description);
        this.unit = new SimpleStringProperty(unit);
        this.totalQuantity = new SimpleIntegerProperty(totalQuantity);
    }

    // Getters for JavaFX properties
    public String getDescription() {
        return description.get();
    }

    public String getUnit() {
        return unit.get();
    }

    public int getTotalQuantity() {
        return totalQuantity.get();
    }
}
