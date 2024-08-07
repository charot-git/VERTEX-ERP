package com.vertex.vos.Utilities;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TextFieldUtils {
    // ... (previous methods remain unchanged)

    public static void addNumericInputRestriction(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    public static void addDoubleInputRestriction(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d*)?")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void addBillionRestriction(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 12) {
                String first12Chars = newValue.substring(0, 12);
                String remainingChars = newValue.substring(12);
                textField.setText(first12Chars + remainingChars.substring(0, Math.max(0, remainingChars.length() - (newValue.length() - 12))));
            }
        });
    }
    public static boolean isNumeric(String text) {
        // Check if the provided text is numeric
        return text.matches("\\d+");
    }

    public static boolean isValidEmail(String text) {
        // Validate the email format using a regular expression
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return text.matches(emailRegex);
    }

    public static boolean isValidDate(String text) {
        // Validate the date format using a regular expression (YYYY-MM-DD)
        String dateRegex = "\\d{4}-\\d{1,2}-\\d{1,2}";
        return text.matches(dateRegex);
    }

    public static void setComboBoxBehavior(ComboBox<?> comboBox) {
        comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // Show the popup only when ComboBox is focused
            if (newValue) {
                comboBox.show();
            } else {
                comboBox.hide();
            }
        });
    }
}
