package com.vertex.vos.Utilities;

import javafx.util.StringConverter;

public class MoneyStringConverter extends StringConverter<Double> {

    @Override
    public String toString(Double value) {
        // Format double to 2 decimal places (e.g., 123.456 -> "123.46")
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value);
    }

    @Override
    public Double fromString(String string) {
        // Parse string to double
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return 0.0; // Default to 0.0 if parsing fails
        }
    }
}
