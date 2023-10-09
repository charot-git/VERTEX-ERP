package com.example.vos;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Optional;

public class signOutAlert {
    public static boolean showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Create custom buttons
        ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonData.NO);

        // Set buttons
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Get the dialog pane and apply CSS styles
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(signOutAlert.class.getResource("/com/example/vos/assets/alert.css").toExternalForm()); // Load your CSS file
        dialogPane.getStyleClass().add("custom-alert");

        // Set stage style and transparency
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);

        Optional<ButtonType> result = alert.showAndWait();

        // Handle the user's choice
        if (result.isPresent() && result.get() == yesButton) {
            // User clicked Yes, perform your action here
            return true;
        } else {
            // User clicked No or closed the dialog, handle accordingly
            return false;
        }
    }
}
