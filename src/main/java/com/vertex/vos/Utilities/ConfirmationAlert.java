package com.vertex.vos.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class ConfirmationAlert {

    private final String title;
    private final String headerText;
    private final String contentText;
    private final boolean isContinueDialog; // New parameter

    public ConfirmationAlert(String title, String headerText, String contentText, boolean isContinueDialog) {
        this.title = title;
        this.headerText = headerText;
        this.contentText = contentText;
        this.isContinueDialog = isContinueDialog;
    }

    public boolean showAndWait() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(signOutAlert.class.getResource("/com/vertex/vos/assets/alert.css").toExternalForm()); // Load your CSS file

        // Apply appropriate style class based on the boolean parameter
        if (isContinueDialog) {
            dialogPane.getStyleClass().add("custom-continue");
        } else {
            dialogPane.getStyleClass().add("custom-alert");
        }

        // Set stage style and transparency
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);

        ButtonType buttonTypeOK = new ButtonType("Yes");
        ButtonType buttonTypeCancel = new ButtonType("Cancel");
        ButtonType no = new ButtonType("No");

        if (isContinueDialog) {
            alert.getButtonTypes().setAll(buttonTypeOK, no);
        } else {
            alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonTypeOK;
    }
}
