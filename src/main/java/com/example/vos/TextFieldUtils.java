package com.example.vos;

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

    public static boolean isNumeric(String text) {
        // Check if the provided text is numeric
        return text.matches("\\d+");
    }

    public static boolean isValidEmail(String text) {
        // Validate the email format using a regular expression
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return text.matches(emailRegex);
    }
}
