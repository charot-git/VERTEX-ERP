package com.vertex.vos.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class EntryAlert {

    public static String showEntryAlert(String title, String headerText, String contentText) {
        TextInputDialog entryAlert = new TextInputDialog();
        entryAlert.setTitle(title);
        entryAlert.setHeaderText(headerText);
        entryAlert.setContentText(contentText);

        Optional<String> result = entryAlert.showAndWait();

        if (result.isPresent()) {
            // Return the entered text if it's present
            return result.get();
        } else {
            // Return an empty string or handle the absence of input as needed
            return ""; // or return null or any other default value
        }
    }

    private static void displayInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
